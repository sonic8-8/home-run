import { sleep } from 'k6';

const DEFAULT_SUMMARY_DIR = 'performance/k6/results';
const DEFAULT_FAILURE_RATE_LIMIT = 0.01;
const DEFAULT_HARD_GATE_P95_MS = 1500;
const DEFAULT_TREND_STATS = ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'];
const SUMMARY_VALUE_ORDER = ['rate', 'avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'];

export {
  DEFAULT_FAILURE_RATE_LIMIT,
  DEFAULT_HARD_GATE_P95_MS,
};

export function getStringEnv(name, defaultValue = '') {
  const value = __ENV[name];

  if (value === undefined || value === null || value === '') {
    return defaultValue;
  }

  return String(value);
}

export function getNumberEnv(name, defaultValue) {
  const rawValue = getStringEnv(name, '');

  if (rawValue === '') {
    return defaultValue;
  }

  const parsed = Number(rawValue);

  if (Number.isFinite(parsed)) {
    return parsed;
  }

  throw new Error(`${name} must be a number. received=${rawValue}`);
}

export function getBooleanEnv(name, defaultValue = false) {
  const rawValue = getStringEnv(name, '');

  if (rawValue === '') {
    return defaultValue;
  }

  return ['1', 'true', 'yes', 'y', 'on'].includes(rawValue.toLowerCase());
}

export function requireEnv(name) {
  const value = getStringEnv(name, '');

  if (value !== '') {
    return value;
  }

  throw new Error(`${name} is required.`);
}

export function getBaseUrl() {
  return requireEnv('K6_BASE_URL');
}

export function buildTestId(scriptName) {
  const explicit = sanitizeValue(getStringEnv('K6_TEST_ID', ''));

  if (explicit !== '') {
    return explicit;
  }

  const buildNumber = sanitizeValue(getStringEnv('BUILD_NUMBER', 'local'));
  const gitShort = sanitizeValue(getStringEnv('GIT_COMMIT_SHORT', getStringEnv('GIT_COMMIT_SHA', 'dev')));

  return `${sanitizeValue(scriptName)}-${buildNumber}-${gitShort}`;
}

export function buildRunTags(scriptName, extraTags = {}) {
  return mergeObjects({
    script: scriptName,
    testid: buildTestId(scriptName),
    build_number: getStringEnv('BUILD_NUMBER', 'local'),
    git_sha: getStringEnv('GIT_COMMIT_SHORT', getStringEnv('GIT_COMMIT_SHA', 'dev')),
  }, extraTags);
}

export function buildThresholds({
  latencyMetricName,
  latencyP95Ms,
  failureMetricName = 'http_req_failed',
  maxFailureRate = DEFAULT_FAILURE_RATE_LIMIT,
  includeFailureRate = true,
  extraThresholds = {},
}) {
  const thresholds = {};

  if (includeFailureRate) {
    thresholds[failureMetricName] = [`rate<${maxFailureRate}`];
  }

  if (latencyMetricName && latencyP95Ms !== undefined && latencyP95Ms !== null) {
    thresholds[latencyMetricName] = [`p(95)<${latencyP95Ms}`];
  }

  return mergeObjects(thresholds, extraThresholds);
}

export function createOptions({
  scriptName,
  vus,
  duration,
  iterations,
  scenarios,
  thresholds = {},
  tags = {},
  discardResponseBodies = getBooleanEnv('K6_DISCARD_RESPONSE_BODIES', false),
  summaryTrendStats = DEFAULT_TREND_STATS,
}) {
  const options = {
    thresholds,
    tags: buildRunTags(scriptName, tags),
    discardResponseBodies,
    summaryTrendStats,
  };

  if (scenarios) {
    options.scenarios = scenarios;
    return options;
  }

  if (vus !== undefined) {
    options.vus = vus;
  }

  if (duration) {
    options.duration = duration;
  }

  if (iterations !== undefined) {
    options.iterations = iterations;
  }

  return options;
}

export function createJsonParams(requestName, token = null, extra = {}) {
  const headers = mergeObjects({
    'Content-Type': 'application/json',
  }, extra.headers || {});

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const params = mergeObjects({}, extra);
  params.headers = headers;
  params.tags = mergeObjects({ name: requestName }, extra.tags || {});

  return params;
}

export function createSummaryHandler(scriptName) {
  return function handleSummary(data) {
    const summaryText = buildSummaryText(scriptName, data);
    const summaryFileBase = `${resolveSummaryDir()}/${buildTestId(scriptName)}`;

    return {
      [`${summaryFileBase}.json`]: JSON.stringify(data, null, 2),
      [`${summaryFileBase}.txt`]: summaryText,
      stdout: `${summaryText}\n`,
    };
  };
}

export function waitForNonEmptyArray(label, supplier, options = {}) {
  const timeoutSeconds = options.timeoutSeconds !== undefined && options.timeoutSeconds !== null
    ? options.timeoutSeconds
    : getNumberEnv('K6_SETUP_WAIT_TIMEOUT_SECONDS', 45);
  const intervalSeconds = options.intervalSeconds !== undefined && options.intervalSeconds !== null
    ? options.intervalSeconds
    : getNumberEnv('K6_SETUP_WAIT_INTERVAL_SECONDS', 2);
  const deadline = Date.now() + (timeoutSeconds * 1000);
  let lastValue = null;
  let lastError = null;

  while (Date.now() <= deadline) {
    try {
      const value = supplier();

      if (Array.isArray(value) && value.length > 0) {
        return value;
      }

      lastValue = value;
      lastError = null;
    } catch (error) {
      lastError = error;
    }

    if (Date.now() >= deadline) {
      break;
    }

    sleep(intervalSeconds);
  }

  if (lastError) {
    throw new Error(`${label} did not become available within ${timeoutSeconds}s: ${lastError.message}`);
  }

  if (Array.isArray(lastValue)) {
    throw new Error(`${label} did not become available within ${timeoutSeconds}s: length=${lastValue.length}`);
  }

  throw new Error(`${label} did not become available within ${timeoutSeconds}s: type=${typeof lastValue}`);
}

function resolveSummaryDir() {
  return trimTrailingSlash(getStringEnv('K6_SUMMARY_DIR', DEFAULT_SUMMARY_DIR));
}

function buildSummaryText(scriptName, data) {
  const rootGroupName = data.root_group && data.root_group.name
    ? data.root_group.name
    : 'default';
  const lines = [
    `script=${scriptName}`,
    `testid=${buildTestId(scriptName)}`,
    `root_group=${rootGroupName}`,
    'metrics:',
  ];

  Object.keys(data.metrics || {})
    .sort()
    .forEach((metricName) => {
      const metric = data.metrics && data.metrics[metricName]
        ? data.metrics[metricName]
        : {};
      lines.push(`- ${metricName}: ${formatMetricValues(metric.values || {})}`);
    });

  return lines.join('\n');
}

function formatMetricValues(values) {
  const keys = orderedMetricKeys(values);

  if (keys.length === 0) {
    return 'no-values';
  }

  return keys
    .map((key) => `${key}=${formatMetricValue(values[key])}`)
    .join(', ');
}

function orderedMetricKeys(values) {
  const knownKeys = SUMMARY_VALUE_ORDER.filter((key) => values[key] !== undefined);
  const customKeys = Object.keys(values)
    .filter((key) => !SUMMARY_VALUE_ORDER.includes(key))
    .sort();

  return knownKeys.concat(customKeys);
}

function formatMetricValue(value) {
  if (typeof value !== 'number') {
    return String(value);
  }

  if (Number.isInteger(value)) {
    return String(value);
  }

  return value.toFixed(2);
}

function mergeObjects(base, extra) {
  const merged = {};
  const sources = [base || {}, extra || {}];

  sources.forEach((source) => {
    Object.keys(source).forEach((key) => {
      merged[key] = source[key];
    });
  });

  return merged;
}

function sanitizeValue(value) {
  return String(value).replace(/[^a-zA-Z0-9._-]+/g, '-');
}

function trimTrailingSlash(value) {
  return value.replace(/\/+$/, '');
}
