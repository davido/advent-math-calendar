import { AppBar, Toolbar, Typography, Box, Stack, Chip } from "@mui/material";
import { useEffect, useMemo, useState } from "react";

function useXmasCountdown(target = new Date("2025-12-24T00:00:00+01:00")) {
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, []);
  return useMemo(() => {
    const diff = Math.max(0, target.getTime() - now);
    const s = Math.floor(diff / 1000);
    const days = Math.floor(s / 86400);
    const hours = Math.floor((s % 86400) / 3600);
    const minutes = Math.floor((s % 3600) / 60);
    const seconds = s % 60;
    const done = diff <= 0;
    return { days, hours, minutes, seconds, done };
  }, [now, target]);
}

export default function FestiveAppBar() {
  const { days, hours, minutes, seconds, done } = useXmasCountdown();

  return (
    <AppBar
      position="sticky"
      color="primary"
      elevation={2}
      sx={{
        background: "linear-gradient(90deg,#b71c1c,#d32f2f)",
        boxShadow: "0 8px 24px rgba(183,28,28,0.35)",
      }}
    >
      <Toolbar>
        <Typography variant="h5" sx={{ flexGrow: 1, fontWeight: 700 }}>
          🎄 Adventskalender 2025
        </Typography>

        <Stack direction="row" spacing={1} alignItems="center" sx={{ mr: 2 }}>
          <Typography variant="body2" sx={{ fontWeight: 700 }}>
            {done ? "Frohe Weihnachten!" : "Countdown:"}
          </Typography>
          {!done && (
            <Chip
              size="small"
              color="secondary"
              label={`${days}d ${hours}h ${minutes}m ${seconds}s`}
              sx={{ fontWeight: 700 }}
            />
          )}
        </Stack>

        <Box component="span" sx={{ fontSize: 24 }}>
          🎅✨
        </Box>
      </Toolbar>
    </AppBar>
  );
}
