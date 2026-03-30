import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

import {
  buildTestId,
  buildThresholds,
  createJsonParams,
  createOptions,
  createSummaryHandler,
  getBaseUrl,
  getNumberEnv,
  getStringEnv,
  waitForNonEmptyArray,
} from './common.js';

const SCRIPT_NAME = 'core-write';
const coreWriteDuration = new Trend('core_write_order_duration', true);
const coreWriteFailureRate = new Rate('core_write_order_failed');

export const options = createOptions({
  scriptName: SCRIPT_NAME,
  vus: getNumberEnv('K6_VUS', 1),
  iterations: getNumberEnv('K6_ITERATIONS', 1),
  thresholds: buildThresholds({
    latencyMetricName: coreWriteDuration.name,
    latencyP95Ms: getNumberEnv('K6_CORE_WRITE_P95_MS', 1500),
    failureMetricName: coreWriteFailureRate.name,
    maxFailureRate: getNumberEnv('K6_CORE_WRITE_MAX_FAILURE_RATE', 0.01),
    includeFailureRate: true,
  }),
  tags: {
    gate: 'core-write',
    endpoint: 'stocks-orders',
  },
});

export function setup() {
  const baseUrl = getBaseUrl();
  const password = getStringEnv('K6_AUTH_PASSWORD', 'Password123!');
  const email = buildSyntheticEmail();

  requestJson('POST', `${baseUrl}/api/auth/signup`, {
    name: 'RR Core Write',
    email,
    password,
    passwordConfirm: password,
    termsAgreed: true,
  }, createJsonParams('auth-signup'));

  const loginResponse = requestJson('POST', `${baseUrl}/api/auth/login`, {
    email,
    password,
  }, createJsonParams('auth-login'));
  const accessToken = loginResponse.data && loginResponse.data.accessToken;

  if (!accessToken) {
    throw new Error('access token extraction failed');
  }

  const authParams = (requestName) => createJsonParams(requestName, accessToken);
  const regions = waitForNonEmptyArray('regions', function fetchRegions() {
    const response = requestJson('GET', `${baseUrl}/api/games/regions`, null, authParams('regions-list'));
    return getNestedArray(response.data, 'regions', 'regions');
  });
  const regionCode = pickNestedValue({ regions }, ['regions', 0, 'regionCode']);

  if (!regionCode) {
    throw new Error('region code extraction failed');
  }

  const districts = waitForNonEmptyArray('districts', function fetchDistricts() {
    const response = requestJson(
      'GET',
      `${baseUrl}/api/games/regions/${regionCode}/districts`,
      null,
      authParams('districts-list'),
    );
    return getNestedArray(response.data, 'districts', 'districts');
  });
  const districtCode = pickNestedValue({ districts }, ['districts', 0, 'districtCode']);

  if (!districtCode) {
    throw new Error('district code extraction failed');
  }

  const properties = waitForNonEmptyArray('properties', function fetchProperties() {
    const response = requestJson(
      'GET',
      `${baseUrl}/api/games/regions/${regionCode}/districts/${districtCode}/properties`,
      null,
      authParams('properties-list'),
    );
    return getNestedArray(response.data, 'properties', 'properties');
  });
  const propertyId = pickNestedValue({ properties }, ['properties', 0, 'propertyId']);

  if (!propertyId) {
    throw new Error('target property id extraction failed');
  }

  const createSessionResponse = requestJson(
    'POST',
    `${baseUrl}/api/games/sessions`,
    {
      slotNumber: 1,
      characterType: 'FEMALE',
      characterName: 'RR Gate 3',
      jobType: 'STARTUP',
      regionCode,
      districtCode,
      targetPropertyId: propertyId,
      useMyData: false,
    },
    authParams('session-create'),
  );
  const sessionId = createSessionResponse.data && createSessionResponse.data.sessionId;

  if (!sessionId) {
    throw new Error('session id extraction failed');
  }

  const marketResponse = requestJson(
    'GET',
    `${baseUrl}/api/games/sessions/${sessionId}/stocks/market`,
    null,
    authParams('stocks-market'),
  );
  const stockCode = selectCheapestStockCode((marketResponse.data && marketResponse.data.stocks) || []);

  if (!stockCode) {
    throw new Error('stock code extraction failed');
  }

  return {
    accessToken,
    baseUrl,
    sessionId,
    stockCode,
  };
}

export default function coreWriteScenario(data) {
  const response = requestJson(
    'POST',
    `${data.baseUrl}/api/games/sessions/${data.sessionId}/stocks/orders`,
    {
      stockCode: data.stockCode,
      orderType: getStringEnv('K6_CORE_WRITE_ORDER_TYPE', 'BUY'),
      quantity: getNumberEnv('K6_CORE_WRITE_ORDER_QUANTITY', 1),
    },
    createJsonParams('stocks-order', data.accessToken),
  );

  const success = check(response.raw, {
    'stock order status is 200': (rawResponse) => rawResponse.status === 200,
    'stock order has order id': () => Number.isFinite(Number(response.data && response.data.orderId)),
  });

  coreWriteDuration.add(response.raw.timings.duration);
  coreWriteFailureRate.add(!success);

  if (!success) {
    throw new Error(`stock order gate failed. status=${response.raw.status}`);
  }
}

export const handleSummary = createSummaryHandler(SCRIPT_NAME);

function buildSyntheticEmail() {
  const testId = buildTestId(SCRIPT_NAME);
  const suffix = String(Date.now()).slice(-8);

  return `rr-core-write-${testId}-${suffix}@example.com`.replace(/[^a-zA-Z0-9@._-]+/g, '-');
}

function requestJson(method, url, payload, params) {
  const response = method === 'GET'
    ? http.get(url, params)
    : http.post(url, JSON.stringify(payload), params);

  let jsonBody = {};
  try {
    jsonBody = response.json();
  } catch (error) {
    throw new Error(`invalid json response. method=${method} url=${url} status=${response.status}`);
  }

  if (response.status < 200 || response.status >= 300) {
    throw new Error(`request failed. method=${method} url=${url} status=${response.status} body=${response.body}`);
  }

  return {
    raw: response,
    data: (jsonBody && jsonBody.data) || null,
    body: jsonBody,
  };
}

function selectCheapestStockCode(stocks) {
  const sortedStocks = stocks
    .filter((stock) => stock && stock.stockCode && Number.isFinite(Number(stock.currentPrice)))
    .sort((left, right) => Number(left.currentPrice) - Number(right.currentPrice));

  return sortedStocks.length > 0 ? sortedStocks[0].stockCode : null;
}

function getNestedArray(source, key, label) {
  const values = source && source[key];

  if (!Array.isArray(values)) {
    throw new Error(`${label} response ${key} is missing`);
  }

  return values;
}

function pickNestedValue(source, path) {
  let current = source;

  for (let index = 0; index < path.length; index += 1) {
    if (current === null || current === undefined) {
      return null;
    }
    current = current[path[index]];
  }

  return current === undefined ? null : current;
}
