export const MAP_MODES = ['new-game', 'loan-apply', 'browse'] as const;

export type MapMode = (typeof MAP_MODES)[number];
