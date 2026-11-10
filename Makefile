up:
	docker compose down  && docker compose up --build

up-v:
	docker compose down -v && docker compose up --build -d


up-nc:
	docker compose down
	docker compose build --no-cache
	docker compose up

up-v-nc:
	docker compose down -v
	docker compose build --no-cache
	docker compose up

down:
	docker compose down

down-v:
	docker compose down -v