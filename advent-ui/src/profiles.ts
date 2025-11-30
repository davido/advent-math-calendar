export type ProfileSlug = "momo" | "trixi" | "lulu";

export interface ProfileInfo {
  slug: ProfileSlug;
  label: string; // UI-Name (nur anonymisiert)
  difficulty: string; // intern für Backend (light/medium/hard)
}

export const PROFILES: Record<ProfileSlug, ProfileInfo> = {
  momo: {
    slug: "momo",
    label: "Momo",
    difficulty: "light",
  },
  trixi: {
    slug: "trixi",
    label: "Trixi",
    difficulty: "medium",
  },
  lulu: {
    slug: "lulu",
    label: "Lulu",
    difficulty: "hard",
  },
};

export function isProfileSlug(value: string | undefined): value is ProfileSlug {
  return value === "momo" || value === "trixi" || value === "lulu";
}
