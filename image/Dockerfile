FROM node:lts-slim

RUN npm install -g jira
RUN mkdir /app

ENTRYPOINT ["jira"]
CMD [ "/app/config.edn" ]