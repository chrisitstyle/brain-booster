up:
	docker compose down -v && docker compose up --build


up-nc:
	docker compose down -v
	docker compose build --no-cache
	docker compose up

down:
	docker compose down -v