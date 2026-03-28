import type { AxiosResponse } from 'axios';

export interface ApiEnvelope<T> {
  data: T;
}

export function unwrapApiData<T>(response: AxiosResponse<ApiEnvelope<T>>): T {
  return response.data.data;
}
