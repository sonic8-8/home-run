package io.ssafy.p.j14c103.homerun.api.service.account;

import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyAccountProjectionService;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserSsafyAccountSyncService {

    private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private final UserAccountRepository userAccountRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final UserAuthContextService userAuthContextService;
    private final SsafyDemandDepositClient ssafyDemandDepositClient;
    private final SeedmoneyAccountProjectionService seedmoneyAccountProjectionService;

    public void syncLinkedAccounts(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final List<UserAccount> linkedAccounts = userAccountRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .filter(account -> account.getAccountType() == AccountType.MAIN || account.getAccountType() == AccountType.SEEDMONEY)
                .toList();
        if (linkedAccounts.isEmpty()) {
            return;
        }

        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);
        final List<Map<String, Object>> accountSnapshots = ssafyDemandDepositClient.inquireAccountList(userKey);
        final Map<String, Map<String, Object>> snapshotByAccountNumber = accountSnapshots.stream()
                .filter(snapshot -> toText(snapshot.get("accountNo")) != null)
                .collect(Collectors.toMap(
                        snapshot -> toText(snapshot.get("accountNo")),
                        snapshot -> snapshot,
                        (left, right) -> right
                ));
        final Set<String> ownAccountNumbers = linkedAccounts.stream()
                .map(UserAccount::getAccountNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (UserAccount account : linkedAccounts) {
            syncAccountTransactions(userKey, userId, account, ownAccountNumbers);
            syncAccountBalance(account, snapshotByAccountNumber.get(account.getAccountNumber()));
            if (account.getAccountType() == AccountType.SEEDMONEY) {
                seedmoneyAccountProjectionService.syncFromUserAccount(account);
            }
        }
    }

    public void advanceSyncBaseline(
            final UserAccount account,
            final String transactionUniqueNo
    ) {
        if (account == null) {
            throw new IllegalArgumentException("계좌 정보는 필수입니다.");
        }
        account.advanceSsafySync(transactionUniqueNo);
        if (account.getAccountType() == AccountType.SEEDMONEY) {
            seedmoneyAccountProjectionService.syncFromUserAccount(account);
        }
    }

    private void syncAccountTransactions(
            final String userKey,
            final Long userId,
            final UserAccount account,
            final Set<String> ownAccountNumbers
    ) {
        final List<Map<String, Object>> history = ssafyDemandDepositClient.inquireTransactionHistory(
                userKey,
                account.getAccountNumber(),
                account.getOpenedAt().toLocalDate().format(BASIC_DATE_FORMATTER),
                LocalDate.now().format(BASIC_DATE_FORMATTER)
        );
        final String latestTransactionUniqueNo = extractLatestTransactionUniqueNo(history);

        if (!Boolean.TRUE.equals(account.getSsafySyncInitialized())) {
            account.initializeSsafySync(latestTransactionUniqueNo);
            return;
        }

        String maxImportedTransactionUniqueNo = account.getLastSyncedSsafyTransactionUniqueNo();
        for (Map<String, Object> transaction : history) {
            final String transactionUniqueNo = toText(transaction.get("transactionUniqueNo"));
            if (!isImportTarget(account, userId, transactionUniqueNo)) {
                continue;
            }

            userAccountTransactionRepository.save(UserAccountTransaction.createSsafySynced(
                    userId,
                    account.getAccountType(),
                    resolveTransactionType(transaction, ownAccountNumbers),
                    toAmount(transaction.get("transactionBalance")),
                    toText(transaction.get("transactionAccountNo")),
                    toText(transaction.get("transactionSummary")),
                    transactionUniqueNo,
                    toCreatedAt(transaction)
            ));
            maxImportedTransactionUniqueNo = maxTransactionUniqueNo(maxImportedTransactionUniqueNo, transactionUniqueNo);
        }

        account.advanceSsafySync(maxImportedTransactionUniqueNo);
    }

    private void syncAccountBalance(
            final UserAccount account,
            final Map<String, Object> snapshot
    ) {
        if (snapshot == null) {
            return;
        }
        account.updateBalance(toAmount(snapshot.get("accountBalance")));
    }

    private boolean isImportTarget(
            final UserAccount account,
            final Long userId,
            final String transactionUniqueNo
    ) {
        if (transactionUniqueNo == null || transactionUniqueNo.isBlank()) {
            return false;
        }
        if (userAccountTransactionRepository.existsByUserIdAndAccountTypeAndSsafyTransactionUniqueNo(
                userId,
                account.getAccountType(),
                transactionUniqueNo
        )) {
            return false;
        }

        final String lastSyncedTransactionUniqueNo = account.getLastSyncedSsafyTransactionUniqueNo();
        if (lastSyncedTransactionUniqueNo == null || lastSyncedTransactionUniqueNo.isBlank()) {
            return true;
        }
        return compareTransactionUniqueNo(transactionUniqueNo, lastSyncedTransactionUniqueNo) > 0;
    }

    private String extractLatestTransactionUniqueNo(final List<Map<String, Object>> history) {
        return history.stream()
                .map(transaction -> toText(transaction.get("transactionUniqueNo")))
                .filter(Objects::nonNull)
                .max(this::compareTransactionUniqueNo)
                .orElse(null);
    }

    private String maxTransactionUniqueNo(
            final String left,
            final String right
    ) {
        if (left == null || left.isBlank()) {
            return right;
        }
        if (right == null || right.isBlank()) {
            return left;
        }
        return compareTransactionUniqueNo(left, right) >= 0 ? left : right;
    }

    private int compareTransactionUniqueNo(
            final String left,
            final String right
    ) {
        try {
            return new BigInteger(left).compareTo(new BigInteger(right));
        } catch (final NumberFormatException exception) {
            return Comparator.nullsFirst(String::compareTo).compare(left, right);
        }
    }

    private AccountTransactionType resolveTransactionType(
            final Map<String, Object> transaction,
            final Set<String> ownAccountNumbers
    ) {
        final String counterpartyAccountNo = toText(transaction.get("transactionAccountNo"));
        if (counterpartyAccountNo != null && ownAccountNumbers.contains(counterpartyAccountNo)) {
            return AccountTransactionType.INTERNAL_TRANSFER;
        }

        final String typeName = toText(transaction.get("transactionTypeName"));
        if (typeName != null && typeName.contains("입금")) {
            return AccountTransactionType.DEPOSIT;
        }
        if (typeName != null && typeName.contains("출금")) {
            return AccountTransactionType.WITHDRAW;
        }

        final String type = toText(transaction.get("transactionType"));
        if ("1".equals(type)) {
            return AccountTransactionType.DEPOSIT;
        }
        return AccountTransactionType.WITHDRAW;
    }

    private LocalDateTime toCreatedAt(final Map<String, Object> transaction) {
        final String date = toText(transaction.get("transactionDate"));
        final String time = toText(transaction.get("transactionTime"));
        if (date == null) {
            return LocalDateTime.now();
        }

        final LocalDate localDate = LocalDate.parse(date, BASIC_DATE_FORMATTER);
        final LocalTime localTime = time == null
                ? LocalTime.MIDNIGHT
                : LocalTime.parse(time, TIME_FORMATTER);
        return LocalDateTime.of(localDate, localTime);
    }

    private Long toAmount(final Object value) {
        final String text = toText(value);
        if (text == null) {
            return 0L;
        }
        return Long.parseLong(text);
    }

    private String toText(final Object value) {
        if (value == null) {
            return null;
        }

        final String text = String.valueOf(value).trim();
        if (text.isBlank()) {
            return null;
        }
        return text;
    }
}
