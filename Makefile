.PHONY: dev test

dev:
	clj -M:dev

test:
	clj -X:test
