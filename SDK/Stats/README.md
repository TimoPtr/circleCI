# What is it?
This module is responsible for computing statistics based on brushing sessions for a profile,
 associated by a profile id.

# Dependencies

- Android Room
- Dagger

## Usage

### Dagger

- Set up a @Component that at least 
-- provides a _Context_ 
-- provides a _org.threeten.bp.Clock_ 
-- includes _StatsModule.class_

Once that's set up, you can use _StatRepository_ and _WeeklyStatCalculator_.

## Tools

### DashboardCalculatorView interface

Responsible for providing the weeklyStat for a given profile

#### Methods
    
```
 /**
  * Get the past weekly stat for a given profile for a given profileId
  *
  * @param profileId the id of the profile to get the stat to.
  * @return a Flowable with the past weekly stats
  */
  fun getPastWeekStats(profileId: Long?): Flowable<WeeklyStat>
    
  /**
   * Calculates the week startDate from an endDate
   *
   * If endDate is after 4AM (Kolibree start time), we want to go back 6 days. Otherwise, we'll go
   * back 7 days
   *
   * Imagine the following scenarios. Both of them use 7 days.
   *
   * 1. Today is Sunday at 11:00 AM
   * · We want to calculate the averages including today's brushings, so we want to take into
   * account brushings starting from Monday's brushings (today - 6.days)
   *
   * 2. Today is Sunday at 3:00 AM
   * · We don't want to take into account today's brushings because from Kolibree's point of view,
   * today is still Saturday, so we need to take into account brushings starting from past Sunday
   * (today - 7.days)
   *
   *
   * @param endDate date to adjust to
   * @return a ZonedDateTime with the adjusted date
   */
  fun adjustedStartDate(endDate: ZonedDateTime): ZonedDateTime

```

For the latest version, please visit : https://confluence.kolibree.com/display/SOF/Stats+module


### StatRepository interface

Manage the stat data into and store them locally into Room
Every time some data into the `BrushingsRepository` is updated, it will emit the brushing data
in this class and update the stat data stored locally accordingly.

#### Methods 

```
/**
 * Get the list of stats from a given date for a given profileId
 *
 * @param startTime the date to get the stat from , a ZonedDateTime object
 * @param profileId the id of the profile to update the stat
 * @return a Flowable with a list of Stats object
 */
fun getStatsSince(startTime: ZonedDateTime, profileId: Long): Flowable<List<Stat>>
```

This snippet of code is written in kotlin.