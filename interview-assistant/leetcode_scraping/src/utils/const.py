import os
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parents[2]
LOG_DIR = os.path.join(ROOT_DIR, 'logs')
DATA_DIR = os.path.join(ROOT_DIR, 'data')
SCRAPPED_PAGES_DIR = os.path.join(DATA_DIR, 'scrapped_discuss_pages')
RESOURCES_DIR = os.path.join(ROOT_DIR, 'resources')
SCRAPING_BEE_API_KEYS_JSON = os.path.join(RESOURCES_DIR,
                                          'scraping_bee_api_keys.json')
DISCUSS_LINKS_DIR = os.path.join(DATA_DIR, 'discuss_links')
DISCUSS_LINKS_JSON = os.path.join(DISCUSS_LINKS_DIR, 'my_dp_links.json')

SCRAPPED_LC_LINKS_DIR = os.path.join(DATA_DIR, 'scrapped_lc_links')
SCRAPPED_LC_LINKS_JSON = os.path.join(SCRAPPED_LC_LINKS_DIR, 'lc_links.json')
SCRAPPED_LC_TASKS_DIR = os.path.join(DATA_DIR, 'scrapped_lc_tasks')
COMPANIES = ['google', 'facebook', 'amazon', 'uber', 'microsoft', 'airbnb']
STAGES = ['phone-screen-2', 'online-assessment', 'onsite']

BASE_DISC_PAGE_URL = 'https://leetcode.com/discuss/interview-question?' \
                     'currentPage=1&orderBy=hot&query='
