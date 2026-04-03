import http from 'k6/http';
import { Rate, Trend } from 'k6/metrics';

import {
  buildThresholds,
  createJsonParams,
  createOptions,
  createSummaryHandler,
  getBaseUrl,
  getNumberEnv,
} from './common.js';

const SCRIPT_NAME = 'external-home';

const dashboardDuration = new Trend('external_home_dashboard_duration', true);
const dashboardFailed = new Rate('external_home_dashboard_failed');
const spendingDuration = new Trend('external_home_spending_duration', true);
const spendingFailed = new Rate('external_home_spending_failed');
const loanRecommendationsDuration = new Trend('external_home_loan_recommendations_duration', true);
const loanRecommendationsFailed = new Rate('external_home_loan_recommendations_failed');

export const options = createOptions({
  scriptName: SCRIPT_NAME,
  vus: getNumberEnv('K6_VUS', 1),
  iterations: getNumberEnv('K6_ITERATIONS', 1),
  thresholds: buildThresholds({
    includeFailureRate: false,
  }),
});

export function setup() {
  const suffix = `${Date.now()}-${Math.floor(Math.random() * 1000000)}`;
  const email = `rr-external-home-${suffix}@example.com`;
  const password = 'Password123!';

  signup(email, password);
  const loginData = login(email, password);
  const accessToken = requireField(loginData, 'accessToken', 'login');

  return { accessToken };
}

export default function externalHomeScenario(data) {
  const accessToken = requireField(data, 'accessToken', 'setup');
  const baseUrl = getBaseUrl();

  const dashboardResponse = http.get(
    `${baseUrl}/api/home/dashboard`,
    createJsonParams('dashboard', accessToken)
  );
  recordResult(dashboardResponse, dashboardDuration, dashboardFailed);

  const spendingResponse = http.get(
    `${baseUrl}/api/home/spending`,
    createJsonParams('spending', accessToken)
  );
  recordResult(spendingResponse, spendingDuration, spendingFailed);

  const loanRecommendationsResponse = http.get(
    `${baseUrl}/api/home/loan-recommendations`,
    createJsonParams('loan-recommendations', accessToken)
  );
  recordResult(
    loanRecommendationsResponse,
    loanRecommendationsDuration,
    loanRecommendationsFailed
  );
}

export const handleSummary = createSummaryHandler(SCRIPT_NAME);

function signup(email, password) {
  const baseUrl = getBaseUrl();
  const response = http.post(
    `${baseUrl}/api/auth/signup`,
    JSON.stringify({
      name: 'RR External Home',
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

function recordResult(response, durationMetric, failureMetric) {
  durationMetric.add(response.timings.duration);
  failureMetric.add(response.status !== 200);
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
