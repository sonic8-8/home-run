export interface SeedmoneyAccountModel {
  bankName: string;
  accountNumber: string;
  balance: number;
}

export interface SeedmoneyTransactionModel {
  transactionId: string;
  remainingBalance: number;
}

export interface SeedmoneyTransferRequestModel {
  toAccountNumber: string;
  amount: number;
}

export interface SeedmoneyDepositRequestModel {
  fromAccountNumber: string;
  amount: number;
}

export interface SeedmoneyCreateRequestModel {
  accountTypeUniqueNo: string;
}
