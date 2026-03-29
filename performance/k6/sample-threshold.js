import { sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

import {
  buildThresholds,
  createOptions,
  createSummaryHandler,
  getBooleanEnv,
  getNumberEnv,
} from './common.js';

const SCRIPT_NAME = 'sample-threshold';
const sampleDuration = new Trend('sample_gate_duration', true);
const sampleFailureRate = new Rate('sample_gate_failed');

export const options = createOptions({
  scriptName: SCRIPT_NAME,
  vus: getNumberEnv('K6_VUS', 1),
  iterations: getNumberEnv('K6_ITERATIONS', 1),
  thresholds: buildThresholds({
    latencyMetricName: sampleDuration.name,
    latencyP95Ms: getNumberEnv('K6_SAMPLE_P95_MS', 200),
    failureMetricName: sampleFailureRate.name,
    maxFailureRate: getNumberEnv('K6_SAMPLE_MAX_FAILURE_RATE', 0.01),
    includeFailureRate: true,
  }),
});

export default function sampleThresholdScenario() {
  const shouldFail = getBooleanEnv('K6_SAMPLE_FORCE_FAILURE', false);
  const durationMs = shouldFail
    ? getNumberEnv('K6_SAMPLE_FAILURE_DURATION_MS', 350)
    : getNumberEnv('K6_SAMPLE_DURATION_MS', 80);

  sampleDuration.add(durationMs);
  sampleFailureRate.add(shouldFail);
  sleep(durationMs / 1000);
}

export const handleSummary = createSummaryHandler(SCRIPT_NAME);
