# What is it?
This module is the single configuration point for network interactions with our backend's Api

It adds appropriate HTTP Headers to requests and recovers from access token expiration issues.

## Usage
It exposes a ready to use singleton Retrofit instance

Any module that depends on Network can instantiate its retrofit interfaces

```kotlin
@Module
internal abstract class AccountNetworkModule {
    @Provides
    @AppScope
    internal fun providesAccountApiService(retrofit: Retrofit): AccountApi {
            return retrofit.create(AccountApi::class.java)
    }
}
```  
