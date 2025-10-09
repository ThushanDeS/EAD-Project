import apiClient from "./client";

export const adminAPI = {
  getAllOperators: async () => {
    const response = await apiClient.get("/users/operators/all");
    return response.data;
  },

  getActiveOperators: async () => {
    const response = await apiClient.get("/users/operators");
    return response.data;
  },

  createUser: async (userData) => {
    const response = await apiClient.post("/users/operators", userData);
    return response.data;
  },

  updateOperator: async (id, userData) => {
    const response = await apiClient.patch(`/users/operators/${id}`, userData);
    return response.data;
  },

  deleteOperator: async (id) => {
    await apiClient.delete(`/users/operators/${id}`);
  },

  deactivateOperator: async (id) => {
    await apiClient.post(`/users/operators/${id}:deactivate`);
  },

  reactivateOperator: async (id) => {
    await apiClient.post(`/users/operators/${id}:reactivate`);
  },
};
