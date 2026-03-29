import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { ROUTES } from '@app/routes';
import { SelectCharacterPage } from '@features/game/presentation/pages/SelectCharacter';
import { useSelectCharacter } from '@features/game/presentation/hooks/useSelectCharacter';

vi.mock('@features/game/presentation/hooks/useSelectCharacter', () => ({
  useSelectCharacter: vi.fn(),
}));

describe('SelectCharacterPage', () => {
  it('falls back to the local asset when the thumbnail image fails to load', () => {
    vi.mocked(useSelectCharacter).mockReturnValue({
      characters: [
        { characterType: 'FEMALE', thumbnailUrl: '/images/characters/female.png' },
        { characterType: 'MALE', thumbnailUrl: '/images/characters/male.png' },
      ],
      loading: false,
      error: null,
    });

    render(
      <MemoryRouter initialEntries={[{ pathname: ROUTES.GAME_SELECT_CHARACTER }]}>
        <Routes>
          <Route path={ROUTES.GAME_SELECT_CHARACTER} element={<SelectCharacterPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const femaleImage = screen.getByRole('img', { name: 'FEMALE' });
    expect(femaleImage).toHaveAttribute('src', '/images/characters/female.png');

    fireEvent.error(femaleImage);

    expect(femaleImage).toHaveAttribute('src', '/assets/images/gcharac.png');
  });
});
