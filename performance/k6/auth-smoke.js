import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

import {
  DEFAULT_FAILURE_RATE_LIMIT,
  DEFAULT_HARD_GATE_P95_MS,
  buildTestId,
  getBooleanEnv,
  buildThresholds,
  createJsonParams,
  createOptions,
  createSummaryHandler,
  getBaseUrl,
  getNumberEnv,
} from './common.js';

const SCRIPT_NAME = 'auth-smoke';
const authFlowDuration = new Trend('auth_gate_duration', true);
const authFlowFailureRate = new Rate('auth_gate_failed');

export const options = createOptions({
  scriptName: SCRIPT_NAME,
  vus: getNumberEnv('K6_VUS', 1),
  iterations: getNumberEnv('K6_ITERATIONS', 1),
  thresholds: buildThresholds({
    latencyMetricName: authFlowDuration.name,
    // The auth smoke gate measures the full signup -> login -> refresh flow in one iteration.
    latencyP95Ms: getNumberEnv('K6_AUTH_THRESHOLD_P95_MS', DEFAULT_HARD_GATE_P95_MS),
    failureMetricName: authFlowFailureRate.name,
    maxFailureRate: getNumberEnv('K6_AUTH_MAX_FAILURE_RATE', DEFAULT_FAILURE_RATE_LIMIT),
    includeFailureRate: true,
    extraThresholds: {
      http_req_duration: [`p(95)<${getNumberEnv('K6_AUTH_HTTP_P95_MS', DEFAULT_HARD_GATE_P95_MS)}`],
      checks: [`rate>=${getNumberEnv('K6_AUTH_CHECK_RATE', 1)}`],
    },
  }),
});

export default function authSmoke() {
  const startedAt = Date.now();
  const signupPayload = buildSignupPayload();
  let flowFailed = false;

  const signupResponse = http.post(
    `${getBaseUrl()}/api/auth/signup`,
    JSON.stringify(signupPayload),
    createJsonParams('auth-signup')
  );

  const signupResult = evaluate(signupResponse, {
    name: 'signup',
    expectedStatus: 201,
    tokenPath: null,
  });
  flowFailed = signupResult.failed || flowFailed;

  const loginResponse = http.post(
    `${getBaseUrl()}/api/auth/login`,
    JSON.stringify({
      email: signupPayload.email,
      password: signupPayload.password,
    }),
    createJsonParams('auth-login')
  );

  const loginResult = evaluate(loginResponse, {
    name: 'login',
    expectedStatus: 200,
    tokenPath: 'refreshToken',
  });
  flowFailed = loginResult.failed || flowFailed;

  const refreshToken = getDataField(loginResult.body, 'refreshToken');
  if (!refreshToken) {
    logFailure('login missing refreshToken', loginResponse);
    flowFailed = true;
  } else {
    const refreshResponse = http.post(
      `${getBaseUrl()}/api/auth/refresh`,
      null,
      createJsonParams('auth-refresh', refreshToken)
    );

    const refreshResult = evaluate(refreshResponse, {
      name: 'refresh',
      expectedStatus: 200,
      tokenPath: 'accessToken',
    });
    flowFailed = refreshResult.failed || flowFailed;
  }

  authFlowDuration.add(Date.now() - startedAt);
  authFlowFailureRate.add(flowFailed);
}

export const handleSummary = createSummaryHandler(SCRIPT_NAME);

function buildSignupPayload() {
  const runStamp = String(Date.now());
  const suffix = `${buildTestId(SCRIPT_NAME)}-${runStamp}-${__VU}-${__ITER}`;
  const password = 'Password123!';

  return {
    name: `k6-${runStamp.slice(-8)}-${__VU}-${__ITER}`,
    email: `k6-${suffix}@example.com`,
    password,
    passwordConfirm: password,
    termsAgreed: true,
  };
}

function evaluate(response, { name, expectedStatus, tokenPath }) {
  const body = parseBody(response, name);
  const expectedTokenValue = tokenPath ? getDataField(body, tokenPath) : true;

  const passed = check(response, {
    [`${name} status=${expectedStatus}`]: (current) => current.status === expectedStatus,
    [`${name} api status field matches`]: () => body !== null && body.status === expectedStatus,
    [`${name} api data exists`]: () => body !== null && body.data !== undefined && body.data !== null,
    [`${name} token field exists`]: () => !tokenPath || Boolean(expectedTokenValue),
  });

  if (!passed) {
    logFailure(name, response);
  }

  return {
    body,
    failed: !passed,
  };
}

function logFailure(name, response) {
  if (!getBooleanEnv('K6_DEBUG_RESPONSES', false)) {
    return;
  }

  console.error(`[${SCRIPT_NAME}] ${name} status=${response.status} body=${response.body}`);
}

function parseBody(response, name) {
  try {
    return response.json();
  } catch (error) {
    logFailure(`${name} invalid json`, response);
    check(response, {
      [`${name} response is valid json`]: () => false,
    });
    return null;
  }
}

function getDataField(body, fieldName) {
  if (!body || !body.data) {
    return null;
  }

  return body.data[fieldName];
}
