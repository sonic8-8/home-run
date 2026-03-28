export interface LoginRequestModel {
  email: string;
  password: string;
}

export interface SignUpRequestModel {
  name: string;
  email: string;
  password: string;
  passwordConfirm: string;
  termsAgreed: boolean;
}

export interface LoginResponseModel {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresIn: number;
  name: string;
}
