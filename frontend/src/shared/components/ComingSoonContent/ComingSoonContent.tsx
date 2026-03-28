import styles from './ComingSoonContent.module.css';

interface ComingSoonContentProps {
  title: string;
}

export function ComingSoonContent({ title }: ComingSoonContentProps) {
  return <div className={styles.root}>{title} 준비 중</div>;
}
