# AllayMc Land Claim Remastered 1.2.1

Clean replacement repo for AllayMc's claim progression plugin.

## Features
- `/allayclaim` main command
- Tier unlocks based on GriefPrevention claim blocks
- Per-claim active perk selection
- Claim Perk Tree GUI
- Claim Status GUI
- Settings GUI
- Whitelist commands for claim perk access
- SQLite storage
- GriefPrevention 16.18.7 reflection hook
- Build artifact upload on GitHub Actions

## Commands
- `/allayclaim`
- `/allayclaim whitelist list`
- `/allayclaim whitelist add <player>`
- `/allayclaim whitelist remove <player>`
- `/claimadmin reload`

## Build
```bash
gradle build
```

Output jar:
`build/libs/AllayMc-LandClaim-Remastered-1.2.1.jar`
