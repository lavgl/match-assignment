.PHONY: dev test build

dev:
	clj -M:dev

test:
	clj -X:test

build:
	clj -T:build uber

clean:
	clj -T:build clean
