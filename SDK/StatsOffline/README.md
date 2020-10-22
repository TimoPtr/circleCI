# What is it?
This module is responsible for aggregating brushing statistics so that we don't have to calculate them on the fly

See epic https://kolibree.atlassian.net/browse/KLTB002-7087

## Timezone
Stats Offline currently works ignoring timezone

Given the following scenario

1. The user lives in Paris and sees stats. He's happy and goes to NY on vacation.

2. On July 31st, Just before boarding a plane to Paris, he opens the app and brushes his teeth at 23:00

3. After landing in Paris, he opens the app and brushes at 5AM

When he's in Paris, should we show the brushing as if it happened on July 31st? Or August 1st?

a) If we take timezone in consideration, both brushings will show on August 1st, since NY@23:00 is Paris@5AM
b) If we ignore timezone, first brushing will show on July 31st and the 2nd one on August 1st

We think the user expects the latter

# Integration

You need to enable StatsOffline manually. To do that

```
... extends Application {
  @Inject StatsOfflineFeatureToggle statsOfflineFeatureToggle;

  onCreate(){
    ...
    
    statsOfflineFeatureToggle.value = true;
  }
}
``` 

## AggregatedStatsRepository

Exposes methods to retrieve aggregated stats.

Inject it where you want to use it and simply invoke one of the exposed methods
