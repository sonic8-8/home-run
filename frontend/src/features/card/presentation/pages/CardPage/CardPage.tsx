import { CardCatalog } from '../../components/CardCatalog';
import styles from './CardPage.module.css';

export function CardPage() {
  return (
    <main className={styles.page} data-testid="card-page">
      <CardCatalog layout="page" />
    </main>
  );
}
