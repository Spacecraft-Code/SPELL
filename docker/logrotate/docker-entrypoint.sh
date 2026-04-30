#!/bin/bash
#


if [ -n "${DELAYED_START}" ]; then
  sleep ${DELAYED_START}
fi

logrotate_croninterval="@daily"

if [ -n "${LOGROTATE_INTERVAL}" ]; then
  case "$LOGROTATE_INTERVAL" in
    hourly)
      logrotate_croninterval='@hourly'
    ;;
    daily)
      logrotate_croninterval='@daily'
    ;;
    weekly)
      logrotate_croninterval='@weekly'
    ;;
    monthly)
      logrotate_croninterval='@monthly'
    ;;
    yearly)
      logrotate_croninterval='@yearly'
    ;;
    *)
      logrotate_croninterval="$LOGROTATE_INTERVAL"
    ;;
  esac
fi

logrotate_cron_timetable="/usr/sbin/logrotate --state=/tmp/logrotate.status /usr/bin/logrotate.d/logrotate.conf"

if [ "$1" = 'cron' ]; then
  exec /usr/bin/go-cron "${logrotate_croninterval}" /bin/bash -c "${logrotate_cron_timetable}"
else 
  exec "$@"
fi

