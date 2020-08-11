import requests
import re
import sdarot.season as season

BASE_URL = "https://sdarot.rocks"
DECODE_PAGE = 'ISO-8859-1'

class Show:

    # This is shared by all instances of sdarot_show
    # The pattern for season in show page
    _season_pattern = '(/watch/(\d+)-.*?/season/(\d+?))"'

    def __init__(self, id, sdarot_client):
        self._id = id
        self._url = f"{BASE_URL}/watch/{id}"
        print("getting the show page")
        self._page = requests.get(
            self._url,
            headers={
                'User-Agent': sdarot_client.user_agent,
                'Origin': BASE_URL,
            },
        )
        # TODO: make sure we didn't get error from request - if do raise exception

        self._find_seasons(sdarot_client)

    def get_cookie(self):
        return self._page.headers['Set-Cookie'].split(';')[0]

    @property
    def seasons(self):
        return self._seasons

    def _find_seasons(self, sdarot_client):
        self._seasons = []
        for season_info in re.findall(self._season_pattern, self._page.content.decode(DECODE_PAGE)):
            self._seasons.append(
                season.Season(
                    self._id,
                    season_info[2],
                    season_info[0],
                    sdarot_client
                )
            )
        print(f"seasons : {len(self._seasons)}")

    def download(self, sdarot_client):
        for season in self._seasons:
            print(f"download season {season.number}")
            season.download(sdarot_client)
