import { Link } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import styles from './NotFoundContent.module.css';

export function NotFoundContent() {
  return (
    <div className={styles.root}>
      <p className={styles.text}>페이지를 찾을 수 없습니다.</p>
      <Link className={styles.link} to={ROUTES.HOME}>
        홈으로 돌아가기
      </Link>
    </div>
  );
}
