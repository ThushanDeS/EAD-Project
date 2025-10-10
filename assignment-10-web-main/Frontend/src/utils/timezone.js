// Utility functions for handling timezone conversions for Sri Lanka (UTC+5:30)

/**
 * Converts a UTC datetime string to Sri Lanka local time
 * @param {string} utcDateString - UTC datetime string from database
 * @returns {string} Formatted local time string
 */
export const formatToSriLankaTime = (utcDateString) => {
  if (!utcDateString) return 'N/A';
  
  // Parse the UTC date
  const utcDate = new Date(utcDateString);
  
  // Add Sri Lanka timezone offset (+5:30)
  const sriLankaOffsetMinutes = 5.5 * 60; // 5.5 hours in minutes
  const localDate = new Date(utcDate.getTime() + (sriLankaOffsetMinutes * 60 * 1000));
  
  // Format to readable string
  return localDate.toLocaleString('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    timeZone: 'UTC' // Use UTC since we already adjusted the time
  });
};

/**
 * Converts a UTC datetime string to Sri Lanka local time for datetime-local input
 * @param {string} utcDateString - UTC datetime string from database
 * @returns {string} Formatted string for datetime-local input (YYYY-MM-DDTHH:MM)
 */
export const formatToDateTimeLocal = (utcDateString) => {
  if (!utcDateString) return '';
  
  // Parse the UTC date
  const utcDate = new Date(utcDateString);
  
  // Add Sri Lanka timezone offset (+5:30)
  const sriLankaOffsetMinutes = 5.5 * 60; // 5.5 hours in minutes
  const localDate = new Date(utcDate.getTime() + (sriLankaOffsetMinutes * 60 * 1000));
  
  // Format to datetime-local format
  const year = localDate.getUTCFullYear();
  const month = String(localDate.getUTCMonth() + 1).padStart(2, '0');
  const day = String(localDate.getUTCDate()).padStart(2, '0');
  const hours = String(localDate.getUTCHours()).padStart(2, '0');
  const minutes = String(localDate.getUTCMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

/**
 * Gets current Sri Lanka time for minimum datetime validation
 * @returns {string} Current local time in datetime-local format
 */
export const getCurrentSriLankaTime = () => {
  const now = new Date();
  
  // Add Sri Lanka timezone offset (+5:30)
  const sriLankaOffsetMinutes = 5.5 * 60; // 5.5 hours in minutes
  const localNow = new Date(now.getTime() + (sriLankaOffsetMinutes * 60 * 1000));
  
  // Format to datetime-local format
  const year = localNow.getUTCFullYear();
  const month = String(localNow.getUTCMonth() + 1).padStart(2, '0');
  const day = String(localNow.getUTCDate()).padStart(2, '0');
  const hours = String(localNow.getUTCHours()).padStart(2, '0');
  const minutes = String(localNow.getUTCMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};