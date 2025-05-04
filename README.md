# User Management Cloud – **zero‑coding deploy**

> Ready‑to‑push Spring Boot OAuth2 app with automatic CI/CD to AWS Elastic Beanstalk.

## _One‑time setup (≈ 15 min)_

1. **Create a private GitHub repo** and push this folder.
2. **Register OAuth2 apps**  
   *GitHub* & **Google** → callback URLs:
   ```
   http://localhost:8080/login/oauth2/code/github
   http://localhost:8080/login/oauth2/code/google
   ```
3. **Create Elastic Beanstalk environment** (Corretto 21).  
   Note `Application name`, `Environment name` and S3 **bucket** EB creates.
4. In **GitHub → Settings → Secrets and variables → Actions → Secrets**, add:

   | Secret | Value |
   |---|---|
   | `AWS_ACCESS_KEY_ID` | IAM user with EB perms |
   | `AWS_SECRET_ACCESS_KEY` | ^ |
   | `AWS_REGION` | e.g. `eu-north-1` |
   | `EB_APP_NAME` | EB application name |
   | `EB_ENV_NAME` | EB environment name |
   | `S3_BUCKET` | EB bucket name |
   | `GITHUB_CLIENT_ID` | Your GitHub OAuth App client‑id |
   | `GITHUB_CLIENT_SECRET` | Your GitHub OAuth App secret |
   | `GOOGLE_CLIENT_ID` | Google client‑id |
   | `GOOGLE_CLIENT_SECRET` | Google secret |

   > **Tip:** For the four OAuth secrets, prefix with `SPRING_` in EB console instead and delete from GitHub if you prefer.

5. **Push → main**.  
   The bundled GitHub Action builds, uploads, and deploys automatically.  
   Wait ⏲️ ~5 minutes for EB to turn **green**.

6. Add the production callback URLs in GitHub & Google (same pattern as above but with your EB domain).

Done! 🎉 You now meet **every VG requirement** without touching code.

---

## Local run (optional)

```bash
cp .env.example .env   # fill in secrets
./mvnw spring-boot:run
```

Open <http://localhost:8080> and sign in.

---

## Manual redeploy (if you dislike CI/CD)

```bash
chmod +x scripts/deploy-eb.sh
AWS_REGION=eu-north-1 EB_ENV_NAME=my-env ./scripts/deploy-eb.sh
```

---

## License

MIT
