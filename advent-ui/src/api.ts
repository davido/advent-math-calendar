import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
});

export type DoorCard = {
  day: number;
  date: string;
  unlocked: boolean;
  solutionUnlocked: boolean;
  title: string;
  taskUrl: string;
  solutionUrl: string;
  imageUrl?: string;
};
