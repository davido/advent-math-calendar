import { useEffect, useRef } from "react";

type SnowfallProps = {
  density?: number;  // Basis-Anzahl Flocken (wird auf 2 Layer verteilt)
  speed?: number;    // Basisgeschwindigkeit
  opacity?: number;  // Maximal-Deckkraft
  zIndex?: number;
};

type Flake = {
  x: number;
  y: number;
  r: number;              // "Größe" der Flocke
  vx: number;
  vy: number;
  a: number;              // Phase für horizontales Wackeln
  aa: number;             // Winkel-Geschwindigkeit
  alpha: number;          // individuelle Deckkraft
  layer: 0 | 1;           // 0 = Hintergrund, 1 = Vordergrund
  rot: number;            // Rotation
  rotSpeed: number;       // Rotationsgeschwindigkeit
};

export default function Snowfall({
  density = 80,
  speed = 0.6,
  opacity = 0.9,
  zIndex = 9999,
}: SnowfallProps) {
  const ref = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = ref.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let w = (canvas.width = window.innerWidth);
    let h = (canvas.height = window.innerHeight);

    const onResize = () => {
      w = canvas.width = window.innerWidth;
      h = canvas.height = window.innerHeight;
    };
    window.addEventListener("resize", onResize);

    const totalFlakes = density * 2; // 2 Layer
    const flakes: Flake[] = [];

    for (let i = 0; i < totalFlakes; i++) {
      const layer: 0 | 1 = i < density ? 0 : 1; // erste Hälfte: Hintergrund, zweite: Vordergrund
      const baseSize = layer === 0 ? 1.2 : 2.5; // Hintergrund kleiner
      const sizeSpread = layer === 0 ? 1.2 : 3.0;

      const r = Math.random() * sizeSpread + baseSize;

      flakes.push({
        x: Math.random() * w,
        y: Math.random() * h,
        r,
        vx: (Math.random() - 0.5) * (layer === 0 ? 0.2 : 0.5),
        vy: (Math.random() * speed + 0.2) * (layer === 0 ? 0.5 : 1.0),
        a: Math.random() * Math.PI * 2,
        aa: Math.random() * 0.015 + 0.003,
        alpha:
          (Math.random() * (opacity - 0.3) + 0.3) *
          (layer === 0 ? 0.7 : 1.0), // Hintergrund etwas transparenter
        layer,
        rot: Math.random() * Math.PI * 2,
        rotSpeed: (Math.random() - 0.5) * 0.01,
      });
    }

    // Hilfsfunktion: eine ❄ zeichnen
    const drawSnowflake = (flake: Flake) => {
      const { x, y, r, rot } = flake;

      ctx.save();
      ctx.translate(x, y);
      ctx.rotate(rot);

      ctx.beginPath();
      // 6 "Arme" im Kreis
      const arms = 6;
      for (let i = 0; i < arms; i++) {
        // Hauptarm
        ctx.moveTo(0, 0);
        ctx.lineTo(r, 0);

        // kleine Seitenäste
        const branchStart = r * 0.4;
        const branchEnd = r * 0.75;

        ctx.moveTo(branchStart, 0);
        ctx.lineTo(branchEnd, r * 0.3);

        ctx.moveTo(branchStart, 0);
        ctx.lineTo(branchEnd, -r * 0.3);

        ctx.rotate((Math.PI * 2) / arms);
      }

      ctx.stroke();
      ctx.restore();
    };

    let raf = 0;

    const tick = () => {
      ctx.clearRect(0, 0, w, h);

      // Hintergrund etwas weicher zeichnen, Vordergrund kräftiger
      for (const layer of [0, 1] as const) {
        if (layer === 0) {
          ctx.lineWidth = 1;
          ctx.shadowBlur = 2;
        } else {
          ctx.lineWidth = 1.4;
          ctx.shadowBlur = 4;
        }
        ctx.strokeStyle = "#ffffff";
        ctx.shadowColor = "rgba(255,255,255,0.9)";

        for (const f of flakes) {
          if (f.layer !== layer) continue;

          // Bewegung aktualisieren
          f.a += f.aa;
          f.rot += f.rotSpeed;

          f.x += f.vx + Math.cos(f.a) * 0.4 * (layer === 0 ? 0.6 : 1.0);
          f.y += f.vy;

          // Wrap-around
          const margin = 20;
          if (f.x < -margin) f.x = w + margin;
          if (f.x > w + margin) f.x = -margin;
          if (f.y > h + margin) {
            f.y = -margin;
            f.x = Math.random() * w;
          }

          ctx.globalAlpha = f.alpha;
          drawSnowflake(f);
        }
      }

      ctx.globalAlpha = 1;
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
