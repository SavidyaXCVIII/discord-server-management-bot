# Panik Mode Discord Bot

A Spring Boot Discord bot for the **Panik Mode** community server. Handles member welcoming, birthday announcements, server booster recognition, and team management.

---

## Features

### Welcome System
- Automatically sends a welcome embed when a new member joins
- Displays the member's ordinal position (1st, 42nd, 103rd...), account age, and join year
- Mentions the admin role so the new member knows who to contact for roles
- Sends a random GIF after the embed for a fun touch
- Trigger manually with `!testwelcome` for testing

### Birthday Announcements
- Stores member birthdays via the `/adduser` slash command
- Runs automatically every day at **7:00 AM (Asia/Colombo)** and announces any birthdays
- Sends a main announcement embed mentioning all birthday members
- Follows up with individual embeds per member showing their avatar and age

### Server Booster Recognition
- Lists all active server boosters with how long they've been boosting
- Shows boost start date using Discord's native timestamps
- Trigger via `/announcements` slash command or the webhook endpoint

### Team Practice
- `/practice` — Moves all members with the **Team Panik Mode** role who are in a voice channel into the designated practice channel instantly

---

## Slash Commands

| Command | Description | Options |
|---------|-------------|---------|
| `/practice` | Moves Panik Mode team members to the practice voice channel | — |
| `/announcements` | Announces all current server boosters | — |
| `/adduser` | Saves a member's birthday to the database | `user`, `day`, `month`, `year` |
| `/greetings` | Shows the bot's current gateway ping | — |

---

## REST Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/webhook/announce-boosters` | Triggers booster announcement from an external system |

---

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.5** — Web, JPA, Validation, Scheduling
- **JDA 5.0.0** — Java Discord API
- **PostgreSQL** — Stores member birthday data
- **Lombok** — Reduces boilerplate
- **Hibernate** — ORM with `ddl-auto=update`

---

## Configuration

All configuration is done via environment variables. Create an `app.env` file (never commit this):

```env
DISCORD_TOKEN=your_token
DISCORD_WELCOME_CHANNEL_ID=your_channel_id
DISCORD_GREETINGS_CHANNEL_ID=your_channel_id
DISCORD_GUILD_ID=your_guild_id
DISCORD_PRACTICE_CHANNEL_ID=your_channel_id
DISCORD_PANIKMODE_TAG_ID=your_role_id
DB_URL=jdbc:postgresql://localhost:5432/esports
DB_USER=esports
DB_PASSWORD=your_password
```

---

## Deployment

**Build:**
```bash
./mvnw package -DskipTests
```

**Run as a systemd service (Linux VPS):**
```bash
# Copy JAR to server
scp target/discord-bot-*.jar user@your-server:/opt/discord-bot/app.jar

# Enable and start
sudo systemctl enable --now discord-bot

# View live logs
sudo journalctl -u discord-bot -f
```

**Minimum server specs:** 1 vCPU / 1 GB RAM / 25 GB disk

---

## Database

A single `DISCORD_USER` table is auto-created by Hibernate on first startup.

| Column | Type | Description |
|--------|------|-------------|
| `id` | bigint (PK) | Auto-increment |
| `discord_id` | varchar(100) | Unique Discord user ID |
| `name` | varchar(250) | Display name |
| `guild_id` | varchar(100) | Server ID |
| `dob` | date | Date of birth |