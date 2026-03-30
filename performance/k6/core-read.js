import http from 'k6/http';
import { group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

import {
  buildThresholds,
  createJsonParams,
  createOptions,
  createSummaryHandler,
  getBaseUrl,
  getNumberEnv,
  getStringEnv,
  waitForNonEmptyArray,
} from './common.js';

const SCRIPT_NAME = 'core-read';
const CORE_READ_DURATION_METRIC = 'core_read_duration';
const CORE_READ_FAILED_METRIC = 'core_read_failed';
const DEFAULT_PASSWORD = 'Password123!';

const coreReadDuration = new Trend(CORE_READ_DURATION_METRIC, true);
const coreReadFailed = new Rate(CORE_READ_FAILED_METRIC);

export const options = createOptions({
  scriptName: SCRIPT_NAME,
  scenarios: {
    coreRead: {
      executor: 'constant-vus',
      vus: getNumberEnv('K6_CORE_READ_VUS', 3),
      duration: getStringEnv('K6_CORE_READ_DURATION', '20s'),
      gracefulStop: '0s',
    },
  },
  thresholds: buildThresholds({
    latencyMetricName: CORE_READ_DURATION_METRIC,
    latencyP95Ms: getNumberEnv('K6_CORE_READ_P95_MS', 500),
    failureMetricName: CORE_READ_FAILED_METRIC,
    maxFailureRate: getNumberEnv('K6_CORE_READ_MAX_FAILURE_RATE', 0.01),
    extraThresholds: {
      http_req_failed: ['rate<0.01'],
    },
  }),
});

export function setup() {
  const baseUrl = getBaseUrl();
  const email = `rr-core-read-${Date.now()}-${Math.floor(Math.random() * 1000000)}@example.com`;
  const signupPayload = JSON.stringify({
    name: 'RR Core Read',
    email,
    password: DEFAULT_PASSWORD,
    passwordConfirm: DEFAULT_PASSWORD,
    termsAgreed: true,
  });
  const loginPayload = JSON.stringify({
    email,
    password: DEFAULT_PASSWORD,
  });

  const signupResponse = http.post(
    `${baseUrl}/api/auth/signup`,
    signupPayload,
    createJsonParams('auth-signup')
  );
  ensureStatus(signupResponse, 201, 'signup');

  const loginResponse = http.post(
    `${baseUrl}/api/auth/login`,
    loginPayload,
    createJsonParams('auth-login')
  );
  ensureStatus(loginResponse, 200, 'login');

  const loginData = unwrapData(loginResponse, 'login');
  const accessToken = loginData.accessToken;

  if (!accessToken) {
    throw new Error('login accessToken is missing');
  }

  const regions = waitForNonEmptyArray('regions', function fetchRegions() {
    const regionsResponse = http.get(
      `${baseUrl}/api/games/regions`,
      authGetParams('games-regions', accessToken)
    );
    ensureStatus(regionsResponse, 200, 'regions');
    return unwrapArrayData(regionsResponse, 'regions', 'regions');
  });
  const regionCode = requireField(regions[0], 'regionCode', 'regions[0]');

  const districts = waitForNonEmptyArray('districts', function fetchDistricts() {
    const districtsResponse = http.get(
      `${baseUrl}/api/games/regions/${regionCode}/districts`,
      authGetParams('games-districts', accessToken)
    );
    ensureStatus(districtsResponse, 200, 'districts');
    return unwrapArrayData(districtsResponse, 'districts', 'districts');
  });
  const districtCode = requireField(districts[0], 'districtCode', 'districts[0]');

  const properties = waitForNonEmptyArray('properties', function fetchProperties() {
    const propertiesResponse = http.get(
      `${baseUrl}/api/games/regions/${regionCode}/districts/${districtCode}/properties`,
      authGetParams('games-properties', accessToken)
    );
    ensureStatus(propertiesResponse, 200, 'properties');
    return unwrapArrayData(propertiesResponse, 'properties', 'properties');
  });
  const propertyId = requireField(properties[0], 'propertyId', 'properties[0]');

  const createSessionPayload = JSON.stringify({
    slotNumber: 1,
    characterType: 'FEMALE',
    characterName: 'RR Core Read',
    jobType: 'STARTUP',
    regionCode,
    districtCode,
    targetPropertyId: propertyId,
    useMyData: false,
  });

  const createSessionResponse = http.post(
    `${baseUrl}/api/games/sessions`,
    createSessionPayload,
    createJsonParams('games-create-session', accessToken)
  );
  ensureStatus(createSessionResponse, 201, 'create-session');
  const sessionId = unwrapData(createSessionResponse, 'create-session').sessionId;

  if (!sessionId) {
    throw new Error('sessionId is missing');
  }

  return {
    accessToken,
    baseUrl,
    sessionId,
  };
}

export default function coreReadScenario(data) {
  group('core-read', function () {
    performRead(
      'turn-actions',
      `${data.baseUrl}/api/games/sessions/${data.sessionId}/turn/actions`,
      authGetParams('core-read-turn-actions', data.accessToken),
      function validateTurnActions(response) {
        const payload = unwrapData(response, 'turn-actions');
        return Array.isArray(payload.shopping) && Array.isArray(payload.activities);
      }
    );

    performRead(
      'stocks-market',
      `${data.baseUrl}/api/games/sessions/${data.sessionId}/stocks/market`,
      authGetParams('core-read-stocks-market', data.accessToken),
      function validateStocksMarket(response) {
        const payload = unwrapData(response, 'stocks-market');
        return Array.isArray(payload.stocks) && payload.stocks.length > 0;
      }
    );

    performRead(
      'loan-products',
      `${data.baseUrl}/api/games/sessions/${data.sessionId}/loans/products?category=ALL&page=0&size=20`,
      authGetParams('core-read-loan-products', data.accessToken),
      function validateLoanProducts(response) {
        const payload = unwrapData(response, 'loan-products');
        return Array.isArray(payload) && payload.length > 0;
      }
    );
  });

  sleep(getNumberEnv('K6_CORE_READ_SLEEP_SECONDS', 1));
}

export const handleSummary = createSummaryHandler(SCRIPT_NAME);

function performRead(label, url, params, validator) {
  const response = http.get(url, params);
  const ok = response.status === 200 && validator(response);

  coreReadDuration.add(response.timings.duration);
  coreReadFailed.add(!ok);

  if (!ok) {
    throw new Error(`${label} failed with status=${response.status}`);
  }
}

function authGetParams(requestName, authorizationHeader) {
  return createJsonParams(requestName, authorizationHeader);
}

function ensureStatus(response, expectedStatus, label) {
  if (response.status === expectedStatus) {
    return;
  }

  throw new Error(`${label} failed with status=${response.status}, body=${response.body}`);
}

function unwrapData(response, label) {
  const parsed = response.json();

  if (!parsed || parsed.data === undefined || parsed.data === null) {
    throw new Error(`${label} response data is missing`);
  }

  return parsed.data;
}

function unwrapArrayData(response, label, key) {
  const data = unwrapData(response, label);
  const values = data && data[key];

  if (!Array.isArray(values)) {
    throw new Error(`${label} response ${key} is missing`);
  }

  return values;
}

function requireField(source, key, label) {
  if (source && source[key] !== undefined && source[key] !== null && source[key] !== '') {
    return source[key];
  }

  throw new Error(`${label} is missing ${key}.`);
}
