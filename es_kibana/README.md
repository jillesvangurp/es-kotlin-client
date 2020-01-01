Simple docker compose file to fire up es & kibana on their normal ports. Doesn't mount any volumes so shutting it down means data is gone.

```
# start
docker-compose up -d

# tail the logs
docker logs -f es_kibana_elasticsearch_1

# shut it down
docker-compose down
```

