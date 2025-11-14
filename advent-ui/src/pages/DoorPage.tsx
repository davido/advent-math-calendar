import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, type DoorCard } from "../api";
import {
  Box,
  Paper,
  Typography,
  Button,
  Stack,
} from "@mui/material";
import FestiveAppBar from "../components/FestiveAppBar";
import Snowfall from "../components/Snowfall";
import useBell from "../components/useBell";

export default function DoorPage() {
  const { day } = useParams();
  const [door, setDoor] = useState<DoorCard | null>(null);
  const nav = useNavigate();
  const bell = useBell(0.7);

  useEffect(() => {
    api
      .get(`/advent/days/${day}`)
      .then((r) => {
        if ((r.data as any)?.error) setDoor(null);
        else setDoor(r.data as DoorCard);
      })
      .catch(() => setDoor(null));
  }, [day]);

  return (
    <>
      <FestiveAppBar />
      <Snowfall density={80} speed={0.6} opacity={0.5} zIndex={0} />

      <Box sx={{ p: 3, position: "relative", zIndex: 1 }}>
        <Button variant="outlined" onClick={() => nav("/calendar")}>
          ← Zurück
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
                Tür {door.day} –{" "}
                {new Date(door.date).toLocaleDateString("de-DE")}
              </Typography>
              {door.imageUrl && (
                <Box sx={{ textAlign: "center", mb: 2 }}>
                  <img
                    src={door.imageUrl}
                    alt=""
                    style={{ maxWidth: 240 }}
                  />
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
