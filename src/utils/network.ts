export const checkNetworkStatus = async (): Promise<boolean> => {
  try {
    // First check the navigator status
    if (!navigator.onLine) return false;
    
    // Double check with a lightweight request
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);
    
    const response = await fetch('https://httpbin.org/get', {
      method: 'GET',
      mode: 'no-cors',
      cache: 'no-store',
      signal: controller.signal
    });
    
    clearTimeout(timeoutId);
    return true;
  } catch (error) {
    console.log('Network check failed:', error);
    return false;
  }
};