import apiClient from './client';

export const operatorAPI = {
  scanQR: async (qrPayload) => {
    const response = await apiClient.post('/operator/scan-qr', { QrPayload: qrPayload });
    return response.data;
  },
  
  finalizeBooking: async (id, data) => {
    const response = await apiClient.post(`/operator/bookings/${id}:finalize`, data);
    return response.data;
  }
};