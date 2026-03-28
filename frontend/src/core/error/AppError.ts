export class AppError extends Error {
  public readonly code: string;

  constructor(
    message: string,
    code: string,
  ) {
    super(message);
    this.name = 'AppError';
    this.code = code;
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class DomainError extends AppError {
  constructor(message: string) {
    super(message, 'DOMAIN_ERROR');
    this.name = 'DomainError';
  }
}

export class NetworkError extends AppError {
  public readonly statusCode: number;

  constructor(
    message: string,
    statusCode: number,
  ) {
    super(message, 'NETWORK_ERROR');
    this.name = 'NetworkError';
    this.statusCode = statusCode;
  }
}

export class UnauthorizedError extends AppError {
  constructor(message = '인증이 필요합니다.') {
    super(message, 'UNAUTHORIZED');
    this.name = 'UnauthorizedError';
  }
}

export class ForbiddenError extends AppError {
  constructor(message = '접근 권한이 없습니다.') {
    super(message, 'FORBIDDEN');
    this.name = 'ForbiddenError';
  }
}

export class ValidationError extends AppError {
  public readonly fields?: Record<string, string>;

  constructor(
    message: string,
    fields?: Record<string, string>,
  ) {
    super(message, 'VALIDATION_ERROR');
    this.name = 'ValidationError';
    this.fields = fields;
  }
}

export class ResponseMappingError extends AppError {
  constructor(message: string) {
    super(message, 'RESPONSE_MAPPING_ERROR');
    this.name = 'ResponseMappingError';
  }
}

export function isDomainError(error: unknown): error is DomainError {
  return error instanceof DomainError;
}

export function isNetworkError(error: unknown): error is NetworkError {
  return error instanceof NetworkError;
}

export function isUnauthorizedError(error: unknown): error is UnauthorizedError {
  return error instanceof UnauthorizedError;
}

export function isValidationError(error: unknown): error is ValidationError {
  return error instanceof ValidationError;
}

export function isResponseMappingError(error: unknown): error is ResponseMappingError {
  return error instanceof ResponseMappingError;
}

export function toErrorMessage(error: unknown): string {
  if (isDomainError(error)) {
    return error.message;
  }

  if (isNetworkError(error)) {
    return error.message;
  }

  if (isUnauthorizedError(error)) {
    return error.message;
  }

  if (isValidationError(error)) {
    return error.message;
  }

  if (isResponseMappingError(error)) {
    return error.message;
  }

  if (error instanceof Error && error.message.trim() !== '') {
    return error.message;
  }

  return '알 수 없는 오류가 발생했습니다.';
}
