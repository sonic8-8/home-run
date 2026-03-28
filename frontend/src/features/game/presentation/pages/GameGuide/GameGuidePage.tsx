import { useLocation, useNavigate } from 'react-router-dom';
import backgroundImg from '@assets/images/background.png';
import { ROUTES } from '@app/routes';
import styles from './GameGuidePage.module.css';

interface GuideLocationState {
  from?: 'start' | 'main';
}

const GUIDE_SECTIONS = [
  {
    title: '게임 목표',
    body: '월 단위로 돈을 관리하고 자산을 늘려 목표 주거를 준비하는 흐름입니다.',
  },
  {
    title: '시작 흐름',
    body: '저장 슬롯 선택 후 캐릭터, 닉네임, 시작 방식을 정하고 게임에 진입합니다.',
  },
  {
    title: '메뉴 사용법',
    body: '대출, 부동산, 카드, 뉴스 메뉴를 오가며 매달 의사결정을 이어갑니다.',
  },
  {
    title: '진행 팁',
    body: '새로하기는 빈 슬롯에서만 시작하고, 이어하기는 진행 중인 슬롯에서만 이어집니다.',
  },
];

export function GameGuidePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const from = ((location.state as GuideLocationState | null) ?? {}).from ?? 'start';
  const backTarget = from === 'main' ? ROUTES.GAME : ROUTES.GAME_START;
  const backLabel = from === 'main' ? '게임으로 돌아가기' : '게임 시작 화면으로 돌아가기';

  return (
    <div
      className={styles.page}
      style={{ backgroundImage: `url(${backgroundImg})` }}
    >
      <div className={styles.overlay} />
      <div className={styles.content}>
        <header className={styles.header}>
          <p className={styles.eyebrow}>GAME GUIDE</p>
          <h1 className={styles.title}>홈런 플레이 가이드</h1>
          <p className={styles.description}>
            시연과 첫 플레이에서 바로 이해할 수 있게 핵심 흐름만 짧게 정리했습니다.
          </p>
        </header>

        <section className={styles.grid}>
          {GUIDE_SECTIONS.map((section) => (
            <article key={section.title} className={styles.card}>
              <h2 className={styles.cardTitle}>{section.title}</h2>
              <p className={styles.cardBody}>{section.body}</p>
            </article>
          ))}
        </section>

        <div className={styles.actions}>
          <button
            type="button"
            className={styles.primaryButton}
            onClick={() => navigate(backTarget)}
          >
            {backLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
