import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { useSelectJob } from '../../hooks/useSelectJob';
import styles from './SelectJob.module.css';

interface LocationState {
  slotNumber?: number;
  characterType?: string;
  characterName?: string;
  useMyData?: boolean;
}

const STAT_ROWS = [
  { key: 'salary',      label: '연봉' },
  { key: 'health',      label: '체력' },
  { key: 'stability',   label: '안정성' },
  { key: 'growthSpeed', label: '성장속도' },
  { key: 'difficulty',  label: '난이도' },
] as const;

export default function SelectJob() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;
  const { currentJob, handlePrev, handleNext } = useSelectJob();

  const handleStart = () => {
    if (!currentJob) return;
    navigate(ROUTES.PROPERTY, {
      state: { ...state, jobType: currentJob.jobType, mode: 'new-game' },
    });
  };

  if (!currentJob) return null;

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <button className={styles.cardNavBtn} onClick={handlePrev} aria-label="이전 직업">
          {'<'}
        </button>

        <div className={styles.statsSection}>
          <p className={styles.jobTitle}>직업: {currentJob.label}</p>
          {STAT_ROWS.map(({ key, label }) => (
            <div className={styles.statRow} key={key}>
              <span className={styles.statLabel}>{label}:</span>
              <div className={styles.barBg}>
                <div
                  className={styles.barFill}
                  style={{ width: `${currentJob.stats[key]}%` }}
                />
              </div>
            </div>
          ))}
        </div>

        <button className={styles.cardNavBtn} onClick={handleNext} aria-label="다음 직업">
          {'>'}
        </button>
      </div>

      <button className={styles.startBtn} onClick={handleStart}>
        START
      </button>
    </div>
  );
}
