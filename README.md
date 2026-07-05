# DealFinder

Find the best product deals from Telugu/Hindi YouTube tech reviews with your own Amazon affiliate links.

## Pipeline

```
User Query
  → query-expansion-service  (rule-based: adds "review", "best", price variants)
  → youtube-service           (searches trusted channels, parses amzn.to links from descriptions)
  → affiliate-service         (expands amzn.to → extracts ASIN → rewrites with your tag)
  → image-service             (Google Custom Search for product images)
  → frontend                  (React product cards with Buy on Amazon button)
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Orchestrates the full pipeline |
| query-expansion-service | 8081 | Rule-based query expansion |
| youtube-service | 8082 | YouTube search + description parsing |
| affiliate-service | 8083 | Amazon link rewriting |
| image-service | 8084 | Google image search |
| frontend | 3000 | React UI |

## Local Development

```bash
cp .env.example .env
# Fill in your API keys in .env

docker-compose up --build
```

Open http://localhost:3000

## Environment Variables

| Variable | Service | Description |
|---|---|---|
| `YOUTUBE_API_KEY` | youtube-service | YouTube Data API v3 key |
| `AMAZON_AFFILIATE_TAG` | affiliate-service | Your Amazon affiliate tag (e.g. yourname-21) |
| `GOOGLE_API_KEY` | image-service | Google Cloud API key with Custom Search API enabled |
| `GOOGLE_SEARCH_ENGINE_ID` | image-service | Programmable Search Engine ID (cx) |

### Setting up product images (Google Custom Search)

`image-service` looks up a product photo by searching Google Images for the product name — no Amazon Product Advertising API needed. If `GOOGLE_API_KEY` / `GOOGLE_SEARCH_ENGINE_ID` aren't set, it silently falls back to best-effort Amazon page scraping (unreliable — Amazon blocks bots), so for real image results you'll want to set these up. Free tier is 100 image searches/day.

1. Go to [Google Programmable Search Engine](https://programmablesearchengine.google.com/) → Create a new search engine.
   - Set "Search the entire web" to ON.
   - Under Settings → Basics, turn **Image search** ON.
   - Copy the **Search engine ID** (this is `cx`) → set as `GOOGLE_SEARCH_ENGINE_ID`.
2. Go to [Google Cloud Console](https://console.cloud.google.com/apis/library/customsearch.googleapis.com) → enable the **Custom Search API** on a project.
3. Create an API key under APIs & Services → Credentials → set as `GOOGLE_API_KEY`.
4. Add both to your `.env` file and restart `image-service`.

Successful lookups are cached in-memory per product name so repeat searches don't burn through the daily quota.

## Deployment (Render)

1. Push to GitHub
2. New → Blueprint → connect repo (render.yaml at root)
3. Set all `sync: false` env vars manually in each service's Environment tab
4. Set `API_GATEWAY_URL` on df-frontend to `df-api-gateway.onrender.com` (no https://)
5. Set all 4 service URLs on df-api-gateway with full `https://` prefix

## Adding More Channels

Edit `youtube-service/src/main/resources/application.yml` and add channel IDs to the `trusted-channels` list.
Find a channel's ID by going to their YouTube page → View Page Source → search for `"channelId"`.
