import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, type DoorCard } from "../api";
import { Box, Paper, Typography, Button, Stack } from "@mui/material";
import FestiveAppBar from "../components/FestiveAppBar";
import Snowfall from "../components/Snowfall";
import useBell from "../components/useBell";
import { isProfileSlug, PROFILES } from "../profiles";

type DoorResponse = DoorCard | { error: string };

export default function DoorPage() {
  const { day, slug } = useParams();
  const [door, setDoor] = useState<DoorCard | null>(null);
  const nav = useNavigate();
  const bell = useBell(0.7);

  useEffect(() => {
    if (!slug || !isProfileSlug(slug)) {
      nav("/", { replace: true });
      return;
    }
    if (!day) return;

    api
      .get<DoorResponse>(`/advent/${slug}/days/${day}`)
      .then((r) => {
        const data = r.data;

        if ("error" in data) {
          setDoor(null);
        } else {
          setDoor(data);
        }
      })
      .catch(() => setDoor(null));
  }, [day, slug, nav]);

  if (!slug || !isProfileSlug(slug)) {
    return null;
  }

  const profile = PROFILES[slug];

  return (
    <>
      <FestiveAppBar />
      <Snowfall density={80} speed={0.6} opacity={0.5} zIndex={0} />

      <Box sx={{ p: 3, position: "relative", zIndex: 1 }}>
        <Button variant="outlined" onClick={() => nav(`/${slug}`)}>
          ← Zurück zum Kalender ({profile.label})
        </Button>
        <Paper
          sx={{
            p: 3,
            mt: 2,
            background: "linear-gradient(180deg,#fff,#fff9f0)",
          }}
        >
          {door ? (
            <>
              <Typography variant="h5" fontWeight={700} gutterBottom>
                {profile.label}: Tür {door.day} – {new Date(door.date).toLocaleDateString("de-DE")}
              </Typography>
              {door.imageUrl && (
                <Box sx={{ textAlign: "center", mb: 2 }}>
                  <img src={door.imageUrl} alt="" style={{ maxWidth: 240 }} />
                </Box>
              )}
              <Stack direction="row" spacing={2}>
                <Button
                  variant="contained"
                  color="secondary"
                  onClick={() => {
                    bell.play();
                    window.open(door.taskUrl, "_blank");
                  }}
                >
                  Aufgabe öffnen 🎯
                </Button>
                <Button
                  variant="outlined"
                  color="primary"
                  disabled={!door.solutionUnlocked}
                  onClick={() => {
                    bell.play();
                    window.open(door.solutionUrl, "_blank");
                  }}
                >
                  {door.solutionUnlocked ? "Lösung anzeigen ✅" : "Lösung ab morgen verfügbar 🔒"}
                </Button>
              </Stack>
            </>
          ) : (
            <Typography color="error">
              Dieses Türchen ist noch gesperrt oder existiert nicht.
            </Typography>
          )}
        </Paper>
      </Box>
    </>
  );
}
