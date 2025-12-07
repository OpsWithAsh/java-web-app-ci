#!/bin/bash
echo "Starting regression tests..."

# Example test runner - replace with your framework
mvn verify -P regression

echo "Regression tests completed."
