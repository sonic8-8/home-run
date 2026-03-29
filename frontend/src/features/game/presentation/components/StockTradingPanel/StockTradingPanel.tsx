import { StockTradingPanel as FeatureStockTradingPanel } from '@features/stock';

interface StockTradingPanelProps {
  sessionId: number;
}

export function StockTradingPanel({
  sessionId,
}: StockTradingPanelProps) {
  return <FeatureStockTradingPanel sessionId={sessionId} />;
}

export default StockTradingPanel;
