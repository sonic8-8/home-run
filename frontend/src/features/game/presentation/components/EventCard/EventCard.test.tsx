import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { EventCard } from './EventCard';

describe('EventCard', () => {
  it('falls back to a placeholder when the event image fails to load', () => {
    render(
      <EventCard
        event={{
          title: '부동산 규제',
          description: '긴축 국면 속에서 부동산 규제 강화 소식이 전해졌습니다.',
          imageSrc: '/images/events/real-estate-regulation.png',
          buttons: [
            {
              actionId: 'confirm',
              label: '확인',
              variant: 'primary',
            },
          ],
        }}
        onAction={vi.fn()}
      />,
    );

    const image = screen.getByRole('img', { name: '부동산 규제' });
    fireEvent.error(image);

    expect(screen.getByLabelText('부동산 규제 이미지 대체 영역')).toBeInTheDocument();
    expect(screen.getByText('EVENT')).toBeInTheDocument();
  });
});
