import { useEffect, useState } from "react";
import { api, type DoorCard } from "../api";
import {
  Box,
  Grid,
  Card,
  CardActionArea,
  CardContent,
  CardMedia,
  Typography,
  Chip,
  Stack,
  Button,
} from "@mui/material";
import FestiveAppBar from "../components/FestiveAppBar";
import Snowfall from "../components/Snowfall";
import { useNavigate } from "react-router-dom";

export default function CalendarPage() {
  const [days, setDays] = useState<DoorCard[]>([]);
  const nav = useNavigate();

  useEffect(() => {
    api.get<DoorCard[]>("/advent/days").then((r) => setDays(r.data));
  }, []);

  return (
    <>
      <FestiveAppBar />

      <Box
        sx={{
          p: 3,
          position: "relative",
          zIndex: 1,
          backgroundImage:
            "radial-gradient(circle at 10% 10%, rgba(255,255,255,0.7) 0, transparent 40%), radial-gradient(circle at 90% 20%, rgba(255,0,0,0.08) 0, transparent 35%)",
        }}
      >
        <Typography variant="h4" gutterBottom fontWeight={700}>
          Jeden Tag ein Türchen – Frohe Weihnachten! 🎁
        </Typography>

        <Grid container spacing={2}>
          {days.map((d) => {
            const unlocked = d.unlocked;
            const solutionUnlocked = d.solutionUnlocked;

            const isLocked = !unlocked;
            const isSolutionUnlocked = unlocked && solutionUnlocked;
            const isUnlockedNoSolution = unlocked && !solutionUnlocked;

            const footerText = isLocked
              ? "Wartet auf den großen Tag ✨"
              : isSolutionUnlocked
                ? "Die Lösung ist freigeschaltet ✅"
                : "Türchen ist freigeschaltet ✨";

            const bgLocked = "linear-gradient(180deg,#fafafa,#f0f0f0)";
            const bgUnlockedSolution = "linear-gradient(180deg,#e8f5e9,#c8e6c9)";
            const bgUnlockedNoSolution = "linear-gradient(180deg,#fffde7,#fff9c4)";

            const boxShadowSolution = "0 0 12px rgba(27,94,32,0.35)";
            const boxShadowNoSolution = "0 0 12px rgba(255,193,7,0.35)";

            return (
              <Grid key={d.day} item xs={12} sm={6} md={4} lg={3}>
                <Card
                  sx={{
                    border: "2px solid #c62828",
                    background: isLocked
                      ? bgLocked
                      : isSolutionUnlocked
                        ? bgUnlockedSolution
                        : bgUnlockedNoSolution,
                    boxShadow: isLocked
                      ? "none"
                      : isSolutionUnlocked
                        ? boxShadowSolution
                        : boxShadowNoSolution,
                    transition: "box-shadow .3s ease",
                    display: "flex",
                    flexDirection: "column",
                    height: "100%",
                    minWidth: 0,
                  }}
                >
                  <CardActionArea
                    disabled={isLocked}
                    onClick={() => nav(`/door/${d.day}`)}
                    sx={{
                      display: "flex",
                      flexDirection: "column",
                      alignItems: "stretch",
                      flexGrow: 1,
                    }}
                  >
                    {d.imageUrl ? (
                      <CardMedia
                        component="img"
                        image={d.imageUrl}
                        alt={`Tür ${d.day}`}
                        sx={{
                          height: 160,
                          objectFit: "cover",
                          p: 2,
                          background: "#fff",
                        }}
                      />
                    ) : (
                      <Box
                        sx={{
                          height: 160,
                          p: 2,
                          background: "#fff",
                        }}
                      />
                    )}

                    <CardContent sx={{ flexGrow: 1 }}>
                      <Stack direction="row" justifyContent="space-between" alignItems="center">
                        <Typography variant="h6" fontWeight={700}>
                          Tür {d.day}
                        </Typography>

                        {/* Chip nutzt jetzt dieselben Farben wie der Button (warning/success) */}
                        <Chip
                          size="small"
                          label={unlocked ? "Freigeschaltet" : "Gesperrt"}
                          color={isLocked ? "default" : isSolutionUnlocked ? "success" : "warning"}
                          variant={isLocked ? "outlined" : "filled"}
                          sx={{
                            fontWeight: 600,
                          }}
                        />
                      </Stack>

                      <Typography variant="body2" color="text.secondary" mt={1}>
                        {new Date(d.date).toLocaleDateString("de-DE")}
                      </Typography>
                    </CardContent>
                  </CardActionArea>

                  <Box sx={{ p: 1.5, pt: 0, textAlign: "center" }}>
                    <Button
                      fullWidth
                      size="small"
                      disabled={isLocked}
                      variant={isLocked ? "outlined" : "contained"}
                      color={isLocked ? "inherit" : isSolutionUnlocked ? "success" : "warning"}
                      sx={{
                        textTransform: "none",
                        whiteSpace: "normal",
                        lineHeight: 1.2,
                        fontSize: "0.75rem",
                        py: 0.5,
                      }}
                    >
                      {footerText}
                    </Button>
                  </Box>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      </Box>
      <Snowfall density={90} speed={0.7} opacity={0.9} zIndex={9999} />
    </>
  );
}
