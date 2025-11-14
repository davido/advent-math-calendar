import { useEffect, useRef } from "react";

export default function Snowfall({
  density = 80,
  speed = 0.6,
  opacity = 0.6,
  zIndex = 0,
}: {
  density?: number;
  speed?: number;
  opacity?: number;
  zIndex?: number;
}) {
  const ref = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = ref.current!;
    const ctx = canvas.getContext("2d")!;
    let w = (canvas.width = window.innerWidth);
    let h = (canvas.height = window.innerHeight);

    const onResize = () => {
      w = canvas.width = window.innerWidth;
      h = canvas.height = window.innerHeight;
    };
    window.addEventListener("resize", onResize);

    type Flake = {
      x: number;
      y: number;
      r: number;
      vx: number;
      vy: number;
      a: number;
      aa: number;
    };
    const flakes: Flake[] = Array.from({ length: density }).map(() => ({
      x: Math.random() * w,
      y: Math.random() * h,
      r: Math.random() * 2.2 + 0.8,
      vx: (Math.random() - 0.5) * 0.6,
      vy: Math.random() * speed + 0.3,
      a: Math.random() * Math.PI * 2,
      aa: Math.random() * 0.02 + 0.005,
    }));

    let raf = 0;
    const tick = () => {
      ctx.clearRect(0, 0, w, h);
      ctx.globalAlpha = opacity;
      ctx.fillStyle = "#fff";

      for (const f of flakes) {
        f.a += f.aa;
        f.x += f.vx + Math.cos(f.a) * 0.2;
        f.y += f.vy;

        if (f.x < -5) f.x = w + 5;
        if (f.x > w + 5) f.x = -5;
        if (f.y > h + 5) {
          f.y = -5;
          f.x = Math.random() * w;
        }

        ctx.beginPath();
        ctx.arc(f.x, f.y, f.r, 0, Math.PI * 2);
        ctx.fill();
      }

      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);

    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener("resize", onResize);
      ctx.clearRect(0, 0, w, h);
    };
  }, [density, speed, opacity]);

  return (
    <canvas
      ref={ref}
      style={{
        position: "fixed",
        inset: 0,
        pointerEvents: "none",
        zIndex,
      }}
      aria-hidden
    />
  );
}