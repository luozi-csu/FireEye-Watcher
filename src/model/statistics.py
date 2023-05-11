class DailyFreq:
    def __init__(
            self,
            date=0,
            freq=0) -> None:
        self.date = date
        self.freq = freq

    def to_json(self):
        return {
            "date": self.date,
            "freq": self.freq
        }

class Statistics:
    def __init__(
            self,
            uid=0,
            overheat_num=0,
            normal_num=0,
            underheat_num=0,
            processing_num=0,
            daily_freqs:list[DailyFreq]=[]) -> None:
        self.uid = uid
        
        self.overheat_num = overheat_num
        self.normal_num = normal_num
        self.underheat_num = underheat_num
        self.processed_num = overheat_num + normal_num + underheat_num
        self.processing_num = processing_num
        self.total_num = self.processed_num + self.processing_num
        self.abnormal_num = overheat_num + underheat_num
        
        self.overheat_rate : float
        self.normal_rate : float
        self.underheat_rate : float
        self.abnormal_rate : float
        if self.processed_num > 0:
            self.overheat_rate = float(overheat_num) / float(self.processed_num)
            self.normal_rate = float(normal_num) / float(self.processed_num)
            self.underheat_rate = float(underheat_num) / float(self.processed_num)
            self.abnormal_rate = float(self.abnormal_num) / float(self.processed_num)

        self.daily_freqs : list[DailyFreq] = daily_freqs
        
    def to_json(self):
        return {
            "uid": self.uid,
            "overheat_num": self.overheat_num,
            "normal_num": self.normal_num,
            "underheat_num": self.underheat_num,
            "processing_num": self.processing_num,
            "abnormal_num": self.abnormal_num,
            "overheat_rate": self.overheat_rate,
            "normal_rate": self.normal_rate,
            "underheat_rate": self.underheat_rate,
            "abnormal_rate": self.abnormal_rate,
            "daily_freqs:": [daily_freq.to_json() for daily_freq in self.daily_freqs]
        }