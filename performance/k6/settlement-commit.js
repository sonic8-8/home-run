import http from 'k6/http';
import { Trend, Rate } from 'k6/metrics';

import {
  buildThresholds,
  createJsonParams,
  createOptions,
  createSummaryHandler,
  getBaseUrl,
  getNumberEnv,
} from './common.js';

const SCRIPT_NAME = 'settlement-commit';
const COMMIT_DURATION_METRIC = 'settlement_commit_duration';
const COMMIT_FAILURE_METRIC = 'settlement_commit_failed';

const settlementCommitDuration = new Trend(COMMIT_DURATION_METRIC, true);
const settlementCommitFailed = new Rate(COMMIT_FAILURE_METRIC);

export const options = createOptions({
  scriptName: SCRIPT_NAME,
  vus: getNumberEnv('K6_VUS', 1),
  iterations: getNumberEnv('K6_ITERATIONS', 1),
  thresholds: buildThresholds({
    latencyMetricName: COMMIT_DURATION_METRIC,
    latencyP95Ms: getNumberEnv('K6_SETTLEMENT_COMMIT_P95_MS', 2000),
    failureMetricName: COMMIT_FAILURE_METRIC,
    maxFailureRate: getNumberEnv('K6_MAX_FAILURE_RATE', 0.01),
    includeFailureRate: true,
    extraThresholds: {
      http_req_failed: [`rate<${getNumberEnv('K6_HTTP_FAILURE_RATE', 0.01)}`],
    },
  }),
});

export default function settlementCommitScenario() {
  const baseUrl = getBaseUrl();
  const fixture = bootstrapSettlementFixture();
  const commitResponse = http.post(
    `${baseUrl}/api/games/sessions/${fixture.sessionId}/turn/commit`,
    '{}',
    createJsonParams('turn-commit', fixture.accessToken)
  );

  settlementCommitDuration.add(commitResponse.timings.duration);

  if (commitResponse.status !== 200) {
    settlementCommitFailed.add(true);
    throw new Error(`turn commit failed: status=${commitResponse.status} body=${commitResponse.body}`);
  }

  settlementCommitFailed.add(false);
}

export const handleSummary = createSummaryHandler(SCRIPT_NAME);

function bootstrapSettlementFixture() {
  const baseUrl = getBaseUrl();
  const suffix = `${Date.now()}-${__VU}-${__ITER}`;
  const email = `rr-settlement-${suffix}@example.com`;
  const password = 'Password123!';

  signup(email, password);
  const loginData = login(email, password);
  const refreshToken = requireField(loginData, 'refreshToken', 'login');
  const accessToken = refresh(refreshToken);

  const regions = getDataArray(
    http.get(
      `${baseUrl}/api/games/regions`,
      createJsonParams('regions', accessToken)
    ),
    'regions'
  );
  const regionCode = requireField(regions[0], 'regionCode', 'regions[0]');

  const districts = getDataArray(
    http.get(
      `${baseUrl}/api/games/regions/${regionCode}/districts`,
      createJsonParams('districts', accessToken)
    ),
    'districts'
  );
  const districtCode = requireField(districts[0], 'districtCode', 'districts[0]');

  const properties = getDataArray(
    http.get(
      `${baseUrl}/api/games/regions/${regionCode}/districts/${districtCode}/properties`,
      createJsonParams('properties', accessToken)
    ),
    'properties'
  );
  const propertyId = requireField(properties[0], 'propertyId', 'properties[0]');

  const sessionData = getDataObject(
    http.post(
      `${baseUrl}/api/games/sessions`,
      JSON.stringify({
        slotNumber: 1,
        characterType: 'FEMALE',
        characterName: 'RR Gate 3',
        jobType: 'STARTUP',
        regionCode,
        districtCode,
        targetPropertyId: propertyId,
        useMyData: false,
      }),
      createJsonParams('create-session', accessToken)
    ),
    'create-session',
    201
  );
  const sessionId = requireField(sessionData, 'sessionId', 'create-session');

  const actionResponse = http.get(
    `${baseUrl}/api/games/sessions/${sessionId}/turn/actions`,
    createJsonParams('turn-actions', accessToken)
  );
  getDataObject(actionResponse, 'turn-actions', 200);

  const slotResponse = http.post(
    `${baseUrl}/api/games/sessions/${sessionId}/turn/slots`,
    JSON.stringify({
      slots: [
        { slotIndex: 0, actionType: 'REST' },
        { slotIndex: 1, actionType: 'REST' },
        { slotIndex: 2, actionType: 'REST' },
      ],
    }),
    createJsonParams('turn-slots', accessToken)
  );
  getDataObject(slotResponse, 'turn-slots', 200);

  return {
    accessToken,
    sessionId,
  };
}

function signup(email, password) {
  const baseUrl = getBaseUrl();
  const response = http.post(
    `${baseUrl}/api/auth/signup`,
    JSON.stringify({
      name: 'RR Settlement',
      email,
      password,
      passwordConfirm: password,
      termsAgreed: true,
    }),
    createJsonParams('signup')
  );

  ensureStatus(response, 'signup', 201);
}

function login(email, password) {
  const baseUrl = getBaseUrl();
  const response = http.post(
    `${baseUrl}/api/auth/login`,
    JSON.stringify({
      email,
      password,
    }),
    createJsonParams('login')
  );

  return getDataObject(response, 'login', 200);
}

function refresh(refreshToken) {
  const baseUrl = getBaseUrl();
  const response = http.post(
    `${baseUrl}/api/auth/refresh`,
    null,
    createJsonParams('refresh', null, {
      headers: {
        Authorization: `Bearer ${refreshToken}`,
      },
    })
  );
  const refreshData = getDataObject(response, 'refresh', 200);

  return requireField(refreshData, 'accessToken', 'refresh');
}

function getDataArray(response, label) {
  const data = getDataObject(response, label, 200);

  if (!Array.isArray(data)) {
    throw new Error(`${label} data must be an array.`);
  }

  if (data.length === 0) {
    throw new Error(`${label} data must not be empty.`);
  }

  return data;
}

function getDataObject(response, label, expectedStatus) {
  ensureStatus(response, label, expectedStatus);
  const payload = parseBody(response, label);

  if (!payload || typeof payload !== 'object') {
    throw new Error(`${label} payload must be an object.`);
  }

  if (!Object.prototype.hasOwnProperty.call(payload, 'data')) {
    throw new Error(`${label} payload is missing data.`);
  }

  return payload.data;
}

function ensureStatus(response, label, expectedStatus) {
  if (response.status === expectedStatus) {
    return;
  }

  throw new Error(`${label} failed: status=${response.status} body=${response.body}`);
}

function parseBody(response, label) {
  try {
    return JSON.parse(response.body);
  } catch (error) {
    throw new Error(`${label} body is not valid JSON: ${error.message}`);
  }
}

function requireField(source, key, label) {
  if (source && source[key] !== undefined && source[key] !== null && source[key] !== '') {
    return source[key];
  }

  throw new Error(`${label} is missing ${key}.`);
}
