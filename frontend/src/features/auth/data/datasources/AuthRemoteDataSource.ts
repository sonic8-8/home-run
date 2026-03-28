import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type {
  LoginRequestModel,
  LoginResponseModel,
  SignUpRequestModel,
} from '../models/AuthModel';

@injectable()
export class AuthRemoteDataSource {
  login(request: LoginRequestModel): Promise<LoginResponseModel> {
    return apiClient.post<ApiEnvelope<LoginResponseModel>>(
      '/auth/login',
      request,
      {
        headers: { Authorization: undefined },
      },
    ).then(unwrapApiData);
  }

  signUp(request: SignUpRequestModel): Promise<void> {
    return apiClient.post(
      '/auth/signup',
      request,
      {
        headers: { Authorization: undefined },
      },
    ).then(() => undefined);
  }
}
