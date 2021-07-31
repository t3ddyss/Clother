import datetime
import math
import random
import time

from ..users.models import User


def generate_random_time():
    min_time = datetime.datetime(year=2021, month=1, day=1)
    max_time = datetime.datetime(year=2021, month=3, day=31)

    min_time_ts = int(time.mktime(min_time.timetuple()))
    max_time_ts = int(time.mktime(max_time.timetuple()))

    random_ts = random.randint(min_time_ts, max_time_ts)
    return datetime.datetime.fromtimestamp(random_ts)


def generate_random_location(x0=55.7541, y0=37.62082, radius=17_500):
    radius_in_degrees = radius / (111.32 * 1000 * math.cos(x0 * (math.pi / 180)))

    u = random.uniform(0, 1)
    v = random.uniform(0, 1)
    w = radius_in_degrees * math.sqrt(u)
    t = 2 * math.pi * v
    x = w * math.cos(t)
    y = w * math.sin(t)

    return [x / math.cos(math.radians(y0)) + x0, y + y0]


def get_random_size():
    sizes = ["XS", "S", "M", "L", "XL"] + [str(x) for x in range(6, 12)]
    return random.choice(sizes)


def get_random_user():
    return random.choice(User.query.all()).id
