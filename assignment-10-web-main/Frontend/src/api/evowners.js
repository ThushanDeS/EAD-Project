import apiClient from './client';

export const evOwnersAPI = {
  search: async (nic) => {
    const response = await apiClient.get('/users/owners', { params: { nic } });
    return response.data;
  },

  create: async (ownerData) => {
    const response = await apiClient.post('/users/owners', ownerData);
    return response.data;
  },
  
  update: async (ownerId, ownerData) => {
    const response = await apiClient.patch(`/users/owners/${ownerId}`, ownerData);
    return response.data;
  },
  
  deactivate: async (ownerId) => {
    const response = await apiClient.post(`/users/owners/${ownerId}:deactivate`);
    return response.data;
  },
  
  activate: async (ownerId) => {
    const response = await apiClient.post(`/users/owners/${ownerId}:reactivate`);
    return response.data;
  }
};