import { useMemo, useRef } from "react";

export default function useBell(volume = 0.6) {
  const audio = useRef<HTMLAudioElement | null>(null);

  return useMemo(
    () => ({
      play: () => {
        if (!audio.current) {
          audio.current = new Audio("/sounds/bell.mp3");
          audio.current.volume = volume;
        }
        audio.current.currentTime = 0;
        audio.current.play().catch(() => {});
      },
    }),
    [volume]
  );
}