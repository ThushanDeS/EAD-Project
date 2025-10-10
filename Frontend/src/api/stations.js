import apiClient from "./client";

export const stationsAPI = {
  create: async (stationData) => {
    const response = await apiClient.post("/stations", stationData);
    return response.data;
  },

  update: async (id, stationData) => {
    const response = await apiClient.patch(`/stations/${id}`, stationData);
    return response.data;
  },

  getAll: async () => {
    const response = await apiClient.get("/stations");
    return response.data;
  },

  deactivate: async (id) => {
    const response = await apiClient.post(`/stations/${id}:deactivate`);
    return response.data;
  },

  reactivate: async (id) => {
    const response = await apiClient.post(`/stations/${id}:reactivate`);
    return response.data;
  },

  getMyStation: async () => {
    const response = await apiClient.get("/stations/me");
    return response.data;
  },
};
